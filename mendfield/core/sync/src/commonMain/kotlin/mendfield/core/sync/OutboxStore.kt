// mendfield/core/sync/src/commonMain/kotlin/mendfield/core/sync/OutboxStore.kt
package mendfield.core.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Where the outbox lives: in memory here; Room or localStorage in the apps. */
interface OutboxStore {
    /** Operations waiting to be sent, oldest first. */
    val pending: Flow<List<PendingOperation>>

    /** Operations the server refused, oldest first. */
    val rejected: Flow<List<RejectedOperation>>

    /** Adds [operation] at the end; an id that is already queued is ignored. */
    suspend fun add(operation: PendingOperation)

    suspend fun oldest(limit: Int): List<PendingOperation>

    /** Removes confirmed operations. */
    suspend fun remove(ids: Set<String>)

    /** Moves refused operations from pending to rejected, in one step. */
    suspend fun reject(rejections: List<RejectedOperation>)

    /** Forgets a rejected operation once the organizer has seen it. */
    suspend fun dismiss(id: String)
}

class InMemoryOutboxStore : OutboxStore {
    private val mutex = Mutex() // reject() changes both lists together
    private val pendingState =
        MutableStateFlow<List<PendingOperation>>(emptyList())
    private val rejectedState =
        MutableStateFlow<List<RejectedOperation>>(emptyList())

    override val pending: StateFlow<List<PendingOperation>> =
        pendingState.asStateFlow()
    override val rejected: StateFlow<List<RejectedOperation>> =
        rejectedState.asStateFlow()

    override suspend fun add(operation: PendingOperation) = mutex.withLock {
        pendingState.update { queue ->
            if (queue.any { it.id == operation.id }) queue
            else queue + operation
        }
    }

    override suspend fun oldest(limit: Int): List<PendingOperation> =
        pendingState.value.take(limit)

    override suspend fun remove(ids: Set<String>) = mutex.withLock {
        pendingState.update { queue -> queue.filterNot { it.id in ids } }
    }

    override suspend fun reject(rejections: List<RejectedOperation>) =
        mutex.withLock {
            val ids = rejections.map { it.pending.id }.toSet()
            pendingState.update { queue -> queue.filterNot { it.id in ids } }
            rejectedState.update { it + rejections }
        }

    override suspend fun dismiss(id: String) = mutex.withLock {
        rejectedState.update { list -> list.filterNot { it.pending.id == id } }
    }
}
