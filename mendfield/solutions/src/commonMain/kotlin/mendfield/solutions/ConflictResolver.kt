// mendfield/solutions/src/commonMain/kotlin/mendfield/solutions/ConflictResolver.kt
package mendfield.solutions

import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import mendfield.core.data.EventRepository
import mendfield.core.data.IntakeRepository
import mendfield.core.data.SlotSuggestion
import mendfield.core.data.WriteResult
import mendfield.core.data.slotSuggestions
import mendfield.core.sync.Operation
import mendfield.core.sync.OutboxStore
import mendfield.core.sync.RejectedOperation

/** Exercise 23.3: the refusals a person can fix by choosing another slot. */
val RESCHEDULABLE = setOf("slot_taken", "visitor_busy")

/**
 * Turns a refused assignment into a choice for the organizer: new slots
 * from the triage rules, computed on the refreshed board, and one call that
 * queues the chosen one and forgets the refusal. Nothing happens by itself.
 */
class ConflictResolver(
    private val events: EventRepository,
    private val intake: IntakeRepository,
    private val store: OutboxStore,
    private val clock: Clock = Clock.System,
) {
    /** Empty when the refusal is not about a slot or the item has one now. */
    suspend fun proposals(rejected: RejectedOperation): List<SlotSuggestion> {
        val op = rejected.pending.operation
        if (op !is Operation.AssignSlot || rejected.code !in RESCHEDULABLE) {
            return emptyList()
        }
        val eventId = op.slotId.substringBefore('.') // slot ids start with it
        val board = events.board(eventId).first() ?: return emptyList()
        if (board.slots.any { it.itemId == op.itemId }) return emptyList()
        val item = board.items.firstOrNull { it.id == op.itemId }
            ?: return emptyList()
        return board.slotSuggestions(item, clock.now())
    }

    /** Queues the new assignment; the refusal goes only if that worked. */
    suspend fun accept(
        rejected: RejectedOperation,
        slotId: String,
    ): WriteResult<*> {
        val op = rejected.pending.operation as? Operation.AssignSlot
            ?: return WriteResult.Refused("Not an assignment.")
        val result = intake.assign(slotId, op.itemId)
        if (result is WriteResult.Saved) store.dismiss(rejected.pending.id)
        return result
    }
}
