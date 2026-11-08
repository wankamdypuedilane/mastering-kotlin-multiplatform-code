// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/CafeCache.kt
package mendfield.core.data

import kotlinx.coroutines.flow.Flow
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.Outcome
import mendfield.core.model.RepairSlot

/** Everything the device knows about one event's board. */
data class EventData(
    val slots: List<RepairSlot> = emptyList(),
    val items: List<Item> = emptyList(),
    val outcomes: List<Outcome> = emptyList(),
)

/**
 * The device copy of the repair café: the single source of truth of the
 * screens. Room implements it on Android, iOS and the desktop; the web and
 * the tests use [InMemoryCafeCache]. Every flow emits again after a write.
 *
 * Orders: events by date and opening time, fixers by name, slots by start
 * then fixer, items by registration time, outcomes by logging time (ties by
 * id), so that every implementation shows the same lists.
 */
interface CafeCache {
    fun events(): Flow<List<Event>>
    fun fixers(): Flow<List<Fixer>>
    fun eventData(eventId: String): Flow<EventData>

    suspend fun event(id: String): Event?
    suspend fun item(id: String): Item?
    suspend fun slot(id: String): RepairSlot?

    /** The server's list of events: unknown ones are added, gone ones removed. */
    suspend fun replaceEvents(events: List<Event>)
    suspend fun replaceFixers(fixers: List<Fixer>)

    /** Swaps one event's slots, items and outcomes in a single transaction. */
    suspend fun replaceEventData(eventId: String, data: EventData)

    // Local changes, mirrored by operations in the outbox.
    suspend fun putItem(item: Item)
    suspend fun assign(slotId: String, itemId: String)
    suspend fun putOutcome(outcome: Outcome)
}

/** The orders promised by [CafeCache], for implementations and tests. */
object CafeOrder {
    val events = compareBy<Event>({ it.date }, { it.opensAt }, { it.id })
    val fixers = compareBy<Fixer>({ it.displayName }, { it.id })
    val slots = compareBy<RepairSlot>({ it.start }, { it.fixerId }, { it.id })
    val items = compareBy<Item>({ it.registeredAt }, { it.id })
    val outcomes = compareBy<Outcome>({ it.loggedAt }, { it.id })
}
