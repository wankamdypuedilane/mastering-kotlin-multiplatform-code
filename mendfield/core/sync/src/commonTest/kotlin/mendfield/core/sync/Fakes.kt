// mendfield/core/sync/src/commonTest/kotlin/mendfield/core/sync/Fakes.kt
package mendfield.core.sync

import kotlin.time.Clock
import kotlin.time.Instant
import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.Outcome
import mendfield.core.model.OutcomeKind

internal val T0 = Instant.parse("2026-11-21T10:00:00Z")

internal class FixedClock(var now: Instant = T0) : Clock {
    override fun now(): Instant = now
}

internal fun item(id: String) = Item(id, "evt-02", ItemCategory.LAMP, "Flickers", "Ada", T0)

internal fun pending(id: String, operation: Operation = Operation.AssignSlot("s-$id", "i-$id")) =
    PendingOperation(id, T0, operation)

internal fun outcome(id: String) = Outcome(id, "i-1", OutcomeKind.FIXED, "New switch", T0)

/**
 * A server in miniature: it applies each operation id once and answers a
 * replay with the stored result, like POST /api/v1/sync. Slot "taken" is
 * always refused. [script] decides what each push returns before (or
 * instead of) applying the batch.
 */
internal class FakeServer : SyncTransport {
    enum class Step { ANSWER, OFFLINE, LOSE_RESPONSE, UNAUTHORIZED }

    val script = ArrayDeque<Step>()
    val results = mutableMapOf<String, OperationResult>()
    val applied = mutableListOf<String>() // ids, once each
    val batches = mutableListOf<List<String>>()
    var inFlight = 0
    var maxInFlight = 0

    override suspend fun push(batch: List<PendingOperation>): PushResult {
        inFlight++
        maxInFlight = maxOf(maxInFlight, inFlight)
        try {
            kotlinx.coroutines.yield() // let a concurrent caller try to overlap
            batches += batch.map { it.id }
            return when (script.removeFirstOrNull() ?: Step.ANSWER) {
                Step.OFFLINE -> PushResult.Transient("offline")
                Step.UNAUTHORIZED -> PushResult.Permanent("Unauthorized")
                Step.LOSE_RESPONSE -> { apply(batch); PushResult.Transient("timeout") }
                Step.ANSWER -> PushResult.Delivered(apply(batch))
            }
        } finally {
            inFlight--
        }
    }

    private fun apply(batch: List<PendingOperation>): List<OperationResult> =
        batch.map { op ->
            results.getOrPut(op.id) {
                val slot = (op.operation as? Operation.AssignSlot)?.slotId
                if (slot == "taken") {
                    OperationResult.Rejected(op.id, "slot_taken", "Slot taken")
                } else {
                    applied += op.id
                    OperationResult.Applied(op.id)
                }
            }
        }
}
