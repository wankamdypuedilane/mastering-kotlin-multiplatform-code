// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/KeyValueOutboxStore.kt
package mendfield.core.data

import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mendfield.core.sync.OperationCodec
import mendfield.core.sync.OutboxStore
import mendfield.core.sync.PendingOperation
import mendfield.core.sync.RejectedOperation

/** The part of the browser's Web Storage the web app uses. */
interface KeyValueStorage {
    fun read(key: String): String?
    fun write(key: String, value: String)
}

class InMemoryStorage : KeyValueStorage {
    private val values = mutableMapOf<String, String>()
    override fun read(key: String): String? = values[key]
    override fun write(key: String, value: String) {
        values[key] = value
    }
}

@Serializable
private data class StoredOperation(
    val createdAt: String,
    val json: String, // OperationCodec.encode: the JSON the server receives
    val code: String? = null, // set once the server has refused it
    val message: String? = null,
)

/**
 * The web's outbox: the whole queue as one JSON value under [key] in
 * localStorage, rewritten after each change (a few hundred operations at
 * most). It survives a reload of the page; it is not shared between
 * browsers or devices, and a cleared browser storage loses it.
 */
class KeyValueOutboxStore(
    private val storage: KeyValueStorage,
    private val key: String = "mendfield.outbox",
) : OutboxStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private val rows = MutableStateFlow(load())
    private val pendingState = MutableStateFlow(pendingOf(rows.value))
    private val rejectedState = MutableStateFlow(rejectedOf(rows.value))

    override val pending: StateFlow<List<PendingOperation>> =
        pendingState.asStateFlow()
    override val rejected: StateFlow<List<RejectedOperation>> =
        rejectedState.asStateFlow()

    override suspend fun add(operation: PendingOperation) = change { list ->
        if (list.any { decode(it).id == operation.id }) list
        else list + StoredOperation(
            operation.createdAt.toString(), OperationCodec.encode(operation))
    }

    override suspend fun oldest(limit: Int): List<PendingOperation> =
        pendingState.value.take(limit)

    override suspend fun remove(ids: Set<String>) = change { list ->
        list.filterNot { it.code == null && decode(it).id in ids }
    }

    override suspend fun reject(rejections: List<RejectedOperation>) =
        change { list ->
            val byId = rejections.associateBy { it.pending.id }
            list.map { row ->
                val r = byId[decode(row).id]
                if (row.code != null || r == null) row
                else row.copy(code = r.code, message = r.message)
            }
        }

    override suspend fun dismiss(id: String) = change { list ->
        list.filterNot { it.code != null && decode(it).id == id }
    }

    private suspend fun change(
        block: (List<StoredOperation>) -> List<StoredOperation>,
    ) = mutex.withLock {
        val next = block(rows.value)
        storage.write(key, json.encodeToString(next))
        rows.value = next
        pendingState.value = pendingOf(next)
        rejectedState.value = rejectedOf(next)
    }

    /** A value this version cannot read starts an empty queue. */
    private fun load(): List<StoredOperation> = storage.read(key)
        ?.let { runCatching { json.decodeFromString<List<StoredOperation>>(it) } }
        ?.getOrNull().orEmpty()

    private fun decode(row: StoredOperation): PendingOperation =
        OperationCodec.decode(row.json, Instant.parse(row.createdAt))

    private fun pendingOf(list: List<StoredOperation>) =
        list.filter { it.code == null }.map(::decode)

    private fun rejectedOf(list: List<StoredOperation>) =
        list.filter { it.code != null }.map {
            RejectedOperation(decode(it), it.code!!, it.message.orEmpty())
        }
}
