// mendfield/core/domain/src/commonMain/kotlin/mendfield/core/domain/QueuePlanner.kt
package mendfield.core.domain

import kotlin.time.Instant
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.RepairSlot

data class Assignment(
    val itemId: String,
    val slotId: String,
    val fixerId: String,
)

data class QueuePlan(
    val assignments: List<Assignment>,
    val unplaced: List<Item>,
)

/**
 * Fits the waiting items of one event into open slots that start at or after
 * [notBefore]: first registered, first served. Each item goes to the best
 * fixer (see [suggestFixers]) who still has a usable slot, in that fixer's
 * earliest one. Nothing is changed: the caller applies the plan.
 */
fun planQueue(
    items: List<Item>,
    fixers: List<Fixer>,
    slots: List<RepairSlot>,
    notBefore: Instant,
): QueuePlan {
    val working = slots.toMutableList()
    val scheduled = slots.mapNotNull { it.itemId }.toSet()
    val waiting = items.filter { it.id !in scheduled }
        .sortedWith(compareBy({ it.registeredAt }, { it.id }))
    val assignments = mutableListOf<Assignment>()
    val unplaced = mutableListOf<Item>()

    for (item in waiting) {
        val eventSlots = working.filter { it.eventId == item.eventId }
        fun usable(slot: RepairSlot) = slot.isOpen && slot.start >= notBefore &&
            checkAssignment(slot, item, eventSlots, items) == null
        val slot = suggestFixers(item.category, fixers, eventSlots)
            .firstNotNullOfOrNull { suggestion ->
                eventSlots
                    .filter { it.fixerId == suggestion.fixer.id && usable(it) }
                    .minByOrNull { it.start }
            }
        if (slot == null) {
            unplaced += item
        } else {
            working[working.indexOf(slot)] = slot.copy(itemId = item.id)
            assignments += Assignment(item.id, slot.id, slot.fixerId)
        }
    }
    return QueuePlan(assignments, unplaced)
}
