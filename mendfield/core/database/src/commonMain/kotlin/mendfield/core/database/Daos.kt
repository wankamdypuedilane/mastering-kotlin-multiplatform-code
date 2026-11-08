// mendfield/core/database/src/commonMain/kotlin/mendfield/core/database/Daos.kt
package mendfield.core.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

/** All three lists of an event, read in one transaction. */
data class EventRows(
    val slots: List<SlotEntity>,
    val items: List<ItemEntity>,
    val outcomes: List<OutcomeEntity>,
)

@Dao
interface CafeDao {
    @Query("SELECT * FROM event ORDER BY date, opens_at, id")
    fun observeEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM fixer ORDER BY display_name, id")
    fun observeFixers(): Flow<List<FixerEntity>>

    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun event(id: String): EventEntity?

    @Query("SELECT * FROM item WHERE id = :id")
    suspend fun item(id: String): ItemEntity?

    @Query("SELECT * FROM slot WHERE id = :id")
    suspend fun slot(id: String): SlotEntity?

    @Query("SELECT * FROM slot WHERE event_id = :eventId ORDER BY start_at, fixer_id, id")
    suspend fun slotsOf(eventId: String): List<SlotEntity>

    @Query("SELECT * FROM item WHERE event_id = :eventId ORDER BY registered_at, id")
    suspend fun itemsOf(eventId: String): List<ItemEntity>

    @Query(
        """
        SELECT outcome.* FROM outcome
        JOIN item ON item.id = outcome.item_id
        WHERE item.event_id = :eventId
        ORDER BY outcome.logged_at, outcome.id
        """,
    )
    suspend fun outcomesOf(eventId: String): List<OutcomeEntity>

    /** One snapshot: never slots of the new refresh with items of the old. */
    @Transaction
    suspend fun rowsOf(eventId: String): EventRows =
        EventRows(slotsOf(eventId), itemsOf(eventId), outcomesOf(eventId))

    @Upsert suspend fun upsertEvents(events: List<EventEntity>)
    @Query("DELETE FROM event WHERE id NOT IN (:keep)")
    suspend fun deleteEventsExcept(keep: List<String>)

    @Transaction
    suspend fun replaceEvents(events: List<EventEntity>) {
        deleteEventsExcept(events.map { it.id })
        upsertEvents(events)
    }

    @Upsert suspend fun upsertFixers(fixers: List<FixerEntity>)
    @Query("DELETE FROM fixer WHERE id NOT IN (:keep)")
    suspend fun deleteFixersExcept(keep: List<String>)

    @Transaction
    suspend fun replaceFixers(fixers: List<FixerEntity>) {
        deleteFixersExcept(fixers.map { it.id })
        upsertFixers(fixers)
    }

    @Query("DELETE FROM outcome WHERE item_id IN (SELECT id FROM item WHERE event_id = :eventId)")
    suspend fun deleteOutcomesOf(eventId: String)
    @Query("DELETE FROM item WHERE event_id = :eventId")
    suspend fun deleteItemsOf(eventId: String)
    @Query("DELETE FROM slot WHERE event_id = :eventId")
    suspend fun deleteSlotsOf(eventId: String)

    @Upsert suspend fun upsertSlots(slots: List<SlotEntity>)
    @Upsert suspend fun upsertItems(items: List<ItemEntity>)
    @Upsert suspend fun upsertOutcomes(outcomes: List<OutcomeEntity>)

    /** All or nothing: observers see the old board or the new one. */
    @Transaction
    suspend fun replaceEventData(eventId: String, rows: EventRows) {
        deleteOutcomesOf(eventId) // while the items still say which event
        deleteItemsOf(eventId)
        deleteSlotsOf(eventId)
        upsertSlots(rows.slots)
        upsertItems(rows.items)
        upsertOutcomes(rows.outcomes)
    }

    @Query("UPDATE slot SET item_id = :itemId WHERE id = :slotId")
    suspend fun assign(slotId: String, itemId: String)
}

data class Refusal(val opId: String, val code: String, val message: String)

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox WHERE rejected_code IS NULL ORDER BY seq")
    fun observePending(): Flow<List<OutboxEntity>>

    @Query("SELECT * FROM outbox WHERE rejected_code IS NOT NULL ORDER BY seq")
    fun observeRejected(): Flow<List<OutboxEntity>>

    @Query("SELECT * FROM outbox WHERE rejected_code IS NULL ORDER BY seq LIMIT :limit")
    suspend fun oldest(limit: Int): List<OutboxEntity>

    /** The unique index on op_id turns a second add into a no-op. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: OutboxEntity)

    @Query("DELETE FROM outbox WHERE rejected_code IS NULL AND op_id IN (:ids)")
    suspend fun deletePending(ids: List<String>)

    @Query(
        """
        UPDATE outbox SET rejected_code = :code, rejected_message = :message
        WHERE op_id = :id AND rejected_code IS NULL
        """,
    )
    suspend fun markRejected(id: String, code: String, message: String)

    /** Every refusal of a batch in one transaction. */
    @Transaction
    suspend fun markAllRejected(refusals: List<Refusal>) =
        refusals.forEach { markRejected(it.opId, it.code, it.message) }

    @Query("DELETE FROM outbox WHERE rejected_code IS NOT NULL AND op_id = :id")
    suspend fun deleteRejected(id: String)
}
