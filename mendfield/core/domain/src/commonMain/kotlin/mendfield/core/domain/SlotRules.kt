// mendfield/core/domain/src/commonMain/kotlin/mendfield/core/domain/SlotRules.kt
package mendfield.core.domain

import mendfield.core.model.Event
import mendfield.core.model.Item
import mendfield.core.model.RepairSlot

/** Why a new slot cannot be added to a fixer's schedule. */
sealed interface SlotProblem {
    data object WrongEvent : SlotProblem
    data object OutsideEventHours : SlotProblem
    data class Overlaps(val slotId: String) : SlotProblem
}

/** Checks [candidate] against the event's hours and the fixer's other slots. */
fun checkNewSlot(
    event: Event,
    existing: List<RepairSlot>,
    candidate: RepairSlot,
): SlotProblem? {
    if (candidate.eventId != event.id) return SlotProblem.WrongEvent
    if (candidate.window !in event.hours()) return SlotProblem.OutsideEventHours
    val clash = existing.firstOrNull {
        it.id != candidate.id && it.fixerId == candidate.fixerId &&
            it.window.overlaps(candidate.window)
    }
    return clash?.let { SlotProblem.Overlaps(it.id) }
}

/** Why an item cannot go into a slot. */
sealed interface AssignmentProblem {
    data object WrongEvent : AssignmentProblem
    data class SlotTaken(val itemId: String) : AssignmentProblem
    data class ItemAlreadyScheduled(val slotId: String) : AssignmentProblem
    /** The visitor already has another item at a table at that time. */
    data class VisitorBusy(val slotId: String) : AssignmentProblem
}

/**
 * Null when [item] may go into [slot]. [eventSlots] and [eventItems] are the
 * current slots and items of the event. Repeating an assignment is not a
 * problem: that is what makes it safe to replay.
 */
fun checkAssignment(
    slot: RepairSlot,
    item: Item,
    eventSlots: List<RepairSlot>,
    eventItems: List<Item>,
): AssignmentProblem? {
    if (slot.eventId != item.eventId) return AssignmentProblem.WrongEvent
    if (slot.itemId == item.id) return null
    slot.itemId?.let { return AssignmentProblem.SlotTaken(it) }
    eventSlots.firstOrNull { it.itemId == item.id }?.let {
        return AssignmentProblem.ItemAlreadyScheduled(it.id)
    }
    val visitor = item.visitorName.visitorKey()
    val sameVisitor = eventItems
        .filter { it.id != item.id && it.visitorName.visitorKey() == visitor }
        .map { it.id }.toSet()
    val busy = eventSlots.firstOrNull {
        it.itemId in sameVisitor && it.window.overlaps(slot.window)
    }
    return busy?.let { AssignmentProblem.VisitorBusy(it.id) }
}

private fun String.visitorKey(): String = trim().lowercase()
