// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/EventBoard.kt
package mendfield.core.data

import kotlin.time.Instant
import mendfield.core.domain.FixerSuggestion
import mendfield.core.domain.checkAssignment
import mendfield.core.domain.suggestFixers
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.Outcome
import mendfield.core.model.RepairSlot

/** One event with its fixers, slots, items and outcomes, as one value. */
data class EventBoard(
    val event: Event,
    val fixers: List<Fixer>,
    val data: EventData,
) {
    val slots: List<RepairSlot> get() = data.slots
    val items: List<Item> get() = data.items
    val outcomes: List<Outcome> get() = data.outcomes
}

/** A slot the triage rules propose for an item, with the fixer's fit. */
data class SlotSuggestion(
    val slot: RepairSlot,
    val suggestion: FixerSuggestion,
)

/**
 * The triage of :core:domain applied to a board: best fixers first, each
 * with their earliest open slot (from [notBefore]) that the assignment rules
 * accept for [item]. At most one slot per fixer, at most [limit] in all.
 */
fun EventBoard.slotSuggestions(
    item: Item,
    notBefore: Instant,
    limit: Int = 3,
): List<SlotSuggestion> =
    suggestFixers(item.category, fixers, slots).mapNotNull { suggestion ->
        slots.filter {
            it.fixerId == suggestion.fixer.id && it.isOpen &&
                it.start >= notBefore &&
                checkAssignment(it, item, slots, items) == null
        }.minByOrNull { it.start }?.let { SlotSuggestion(it, suggestion) }
    }.take(limit)

/** Before an item exists: who would get an item of [category] here. */
fun EventBoard.fixerSuggestions(category: ItemCategory): List<FixerSuggestion> =
    suggestFixers(category, fixers, slots)
