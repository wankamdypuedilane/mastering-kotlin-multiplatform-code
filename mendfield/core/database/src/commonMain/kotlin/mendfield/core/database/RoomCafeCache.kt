// mendfield/core/database/src/commonMain/kotlin/mendfield/core/database/RoomCafeCache.kt
package mendfield.core.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import mendfield.core.data.CafeCache
import mendfield.core.data.EventData
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.Outcome
import mendfield.core.model.RepairSlot

/** [CafeCache] over Room: the device copy survives restarts and reboots. */
class RoomCafeCache(private val db: MendfieldDatabase) : CafeCache {
    private val dao = db.cafeDao()

    override fun events(): Flow<List<Event>> =
        dao.observeEvents().map { rows -> rows.map { it.toModel() } }

    override fun fixers(): Flow<List<Fixer>> =
        dao.observeFixers().map { rows -> rows.map { it.toModel() } }

    /**
     * Re-read after any write to the three tables, as one transaction; equal
     * results (a write to another event) are not emitted again.
     */
    override fun eventData(eventId: String): Flow<EventData> =
        db.invalidationTracker.createFlow("slot", "item", "outcome")
            .map { dao.rowsOf(eventId).toModel() }
            .distinctUntilChanged()

    override suspend fun event(id: String): Event? = dao.event(id)?.toModel()
    override suspend fun item(id: String): Item? = dao.item(id)?.toModel()
    override suspend fun slot(id: String): RepairSlot? = dao.slot(id)?.toModel()

    override suspend fun replaceEvents(events: List<Event>) =
        dao.replaceEvents(events.map { it.toEntity() })

    override suspend fun replaceFixers(fixers: List<Fixer>) =
        dao.replaceFixers(fixers.map { it.toEntity() })

    override suspend fun replaceEventData(eventId: String, data: EventData) =
        dao.replaceEventData(eventId, data.toRows())

    override suspend fun putItem(item: Item) = dao.upsertItems(listOf(item.toEntity()))
    override suspend fun assign(slotId: String, itemId: String) =
        dao.assign(slotId, itemId)
    override suspend fun putOutcome(outcome: Outcome) =
        dao.upsertOutcomes(listOf(outcome.toEntity()))
}
