// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/InMemoryCafeCache.kt
package mendfield.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.Outcome
import mendfield.core.model.RepairSlot

/**
 * A [CafeCache] in memory: the web app's cache (gone when the tab closes)
 * and the cache of the tests. One immutable snapshot in a StateFlow, so a
 * replace is atomic and every reader sees a consistent state.
 */
class InMemoryCafeCache : CafeCache {
    private data class Snapshot(
        val events: Map<String, Event> = emptyMap(),
        val fixers: Map<String, Fixer> = emptyMap(),
        val slots: Map<String, RepairSlot> = emptyMap(),
        val items: Map<String, Item> = emptyMap(),
        val outcomes: Map<String, Outcome> = emptyMap(),
    )

    private val state = MutableStateFlow(Snapshot())

    override fun events(): Flow<List<Event>> = state
        .map { it.events.values.sortedWith(CafeOrder.events) }
        .distinctUntilChanged()

    override fun fixers(): Flow<List<Fixer>> = state
        .map { it.fixers.values.sortedWith(CafeOrder.fixers) }
        .distinctUntilChanged()

    override fun eventData(eventId: String): Flow<EventData> = state
        .map { it.dataOf(eventId) }
        .distinctUntilChanged()

    override suspend fun event(id: String): Event? = state.value.events[id]
    override suspend fun item(id: String): Item? = state.value.items[id]
    override suspend fun slot(id: String): RepairSlot? = state.value.slots[id]

    override suspend fun replaceEvents(events: List<Event>) =
        state.update { it.copy(events = events.associateBy { e -> e.id }) }

    override suspend fun replaceFixers(fixers: List<Fixer>) =
        state.update { it.copy(fixers = fixers.associateBy { f -> f.id }) }

    override suspend fun replaceEventData(eventId: String, data: EventData) =
        state.update { s ->
            val oldItems = s.items.values.filter { it.eventId == eventId }
                .map { it.id }.toSet()
            s.copy(
                slots = s.slots.filterValues { it.eventId != eventId } +
                    data.slots.associateBy { it.id },
                items = s.items.filterValues { it.eventId != eventId } +
                    data.items.associateBy { it.id },
                outcomes = s.outcomes.filterValues { it.itemId !in oldItems } +
                    data.outcomes.associateBy { it.id },
            )
        }

    override suspend fun putItem(item: Item) =
        state.update { it.copy(items = it.items + (item.id to item)) }

    override suspend fun assign(slotId: String, itemId: String) =
        state.update { s ->
            val slot = s.slots[slotId] ?: return@update s
            s.copy(slots = s.slots + (slotId to slot.copy(itemId = itemId)))
        }

    override suspend fun putOutcome(outcome: Outcome) =
        state.update { it.copy(outcomes = it.outcomes + (outcome.id to outcome)) }

    private fun Snapshot.dataOf(eventId: String): EventData {
        val eventItems = items.values.filter { it.eventId == eventId }
        val ids = eventItems.map { it.id }.toSet()
        return EventData(
            slots = slots.values.filter { it.eventId == eventId }
                .sortedWith(CafeOrder.slots),
            items = eventItems.sortedWith(CafeOrder.items),
            outcomes = outcomes.values.filter { it.itemId in ids }
                .sortedWith(CafeOrder.outcomes),
        )
    }
}
