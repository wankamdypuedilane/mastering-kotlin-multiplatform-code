// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/IntakeRepository.kt
package mendfield.core.data

import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import mendfield.core.domain.AssignmentProblem
import mendfield.core.domain.checkAssignment
import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.RepairSlot
import mendfield.core.sync.Operation

/** Registering items and putting them into slots: device first, then outbox. */
class IntakeRepository(
    private val cache: CafeCache,
    private val writer: LocalWriter,
    private val clock: Clock = Clock.System,
) {
    suspend fun registerItem(
        eventId: String,
        category: ItemCategory,
        description: String,
        visitorName: String,
    ): WriteResult<Item> {
        if (draftProblems(description, visitorName).isNotEmpty()) {
            return WriteResult.Refused("Check the description and the name.")
        }
        if (cache.event(eventId) == null) {
            return WriteResult.Refused("Unknown event $eventId.")
        }
        val item = Item(
            id = writer.newEntityId(), // keeps this id on the server
            eventId = eventId,
            category = category,
            description = description.trim(),
            visitorName = visitorName.trim(),
            registeredAt = clock.now(),
        )
        writer.write(Operation.RegisterItem(item))
        return WriteResult.Saved(item)
    }

    /**
     * Checks the rules of :core:domain against the device copy. The server
     * checks again when the outbox arrives: first writer wins there.
     */
    suspend fun assign(slotId: String, itemId: String): WriteResult<RepairSlot> {
        val slot = cache.slot(slotId) ?: return WriteResult.Refused("Unknown slot.")
        val item = cache.item(itemId) ?: return WriteResult.Refused("Unknown item.")
        val data = cache.eventData(slot.eventId).first()
        checkAssignment(slot, item, data.slots, data.items)?.let {
            return WriteResult.Refused(it.explain())
        }
        writer.write(Operation.AssignSlot(slotId, itemId))
        return WriteResult.Saved(slot.copy(itemId = itemId))
    }
}

fun AssignmentProblem.explain(): String = when (this) {
    AssignmentProblem.WrongEvent -> "The slot belongs to another event."
    is AssignmentProblem.SlotTaken -> "The slot already holds another item."
    is AssignmentProblem.ItemAlreadyScheduled -> "The item already has a slot."
    is AssignmentProblem.VisitorBusy ->
        "The visitor has another item at a table then."
}
