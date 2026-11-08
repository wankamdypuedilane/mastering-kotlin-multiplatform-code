// mendfield/core/sync/src/commonMain/kotlin/mendfield/core/sync/OperationCodec.kt
package mendfield.core.sync

import kotlin.time.Instant
import mendfield.core.network.AssignSlotOp
import mendfield.core.network.LogOutcomeOp
import mendfield.core.network.MendfieldJson
import mendfield.core.network.RegisterItemOp
import mendfield.core.network.SyncOperationDto
import mendfield.core.network.toDto
import mendfield.core.network.toModel

/** The wire form of an operation: the same JSON the server receives. */
fun PendingOperation.toDto(): SyncOperationDto = when (val op = operation) {
    is Operation.RegisterItem -> RegisterItemOp(id, op.item.toDto())
    is Operation.AssignSlot -> AssignSlotOp(id, op.slotId, op.itemId)
    is Operation.LogOutcome -> LogOutcomeOp(id, op.outcome.toDto())
}

/**
 * Text form for persistent stores (a Room column, localStorage): the
 * operation as JSON; [PendingOperation.createdAt] is stored next to it.
 */
object OperationCodec {
    private val serializer = SyncOperationDto.serializer()

    fun encode(pending: PendingOperation): String =
        MendfieldJson.encodeToString(serializer, pending.toDto())

    fun decode(json: String, createdAt: Instant): PendingOperation {
        val dto = MendfieldJson.decodeFromString(serializer, json)
        val operation = when (dto) {
            is RegisterItemOp -> Operation.RegisterItem(dto.item.toModel())
            is AssignSlotOp -> Operation.AssignSlot(dto.slotId, dto.itemId)
            is LogOutcomeOp -> Operation.LogOutcome(dto.outcome.toModel())
        }
        return PendingOperation(dto.id, createdAt, operation)
    }
}
