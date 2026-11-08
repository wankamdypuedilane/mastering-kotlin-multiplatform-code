// mendfield/core/database/src/commonMain/kotlin/mendfield/core/database/RoomOutboxStore.kt
package mendfield.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mendfield.core.sync.OperationCodec
import mendfield.core.sync.OutboxStore
import mendfield.core.sync.PendingOperation
import mendfield.core.sync.RejectedOperation

/** The outbox of part 1, kept in SQLite: queued work survives a restart. */
class RoomOutboxStore(db: MendfieldDatabase) : OutboxStore {
    private val dao = db.outboxDao()

    override val pending: Flow<List<PendingOperation>> =
        dao.observePending().map { rows -> rows.map { it.toPending() } }

    override val rejected: Flow<List<RejectedOperation>> =
        dao.observeRejected().map { rows ->
            rows.map {
                RejectedOperation(it.toPending(), it.rejectedCode!!,
                    it.rejectedMessage.orEmpty())
            }
        }

    override suspend fun add(operation: PendingOperation) = dao.insert(
        OutboxEntity(opId = operation.id, createdAt = operation.createdAt,
            payload = OperationCodec.encode(operation)),
    )

    override suspend fun oldest(limit: Int): List<PendingOperation> =
        dao.oldest(limit).map { it.toPending() }

    override suspend fun remove(ids: Set<String>) = dao.deletePending(ids.toList())

    override suspend fun reject(rejections: List<RejectedOperation>) =
        dao.markAllRejected(rejections.map {
            Refusal(it.pending.id, it.code, it.message)
        })

    override suspend fun dismiss(id: String) = dao.deleteRejected(id)

    private fun OutboxEntity.toPending(): PendingOperation =
        OperationCodec.decode(payload, createdAt)
}
