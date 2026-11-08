// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/OutcomeRepository.kt
package mendfield.core.data

import kotlin.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import mendfield.core.domain.OutcomeStats
import mendfield.core.domain.outcomeStats
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.Outcome
import mendfield.core.model.RepairSlot
import mendfield.core.model.OutcomeKind
import mendfield.core.sync.Operation

data class ItemHistory(
    val item: Item,
    val event: Event?,
    val slot: RepairSlot?,
    val fixer: Fixer?,
    /** Oldest first: the last one is what the statistics count. */
    val outcomes: List<Outcome>,
)

/** Outcomes are append-only, so logging one offline never conflicts. */
class OutcomeRepository(
    private val cache: CafeCache,
    private val writer: LocalWriter,
    private val clock: Clock = Clock.System,
    private val compute: CoroutineDispatcher = Dispatchers.Default,
) {
    suspend fun logOutcome(
        itemId: String,
        kind: OutcomeKind,
        note: String,
    ): WriteResult<Outcome> {
        if (note.trim().length > ItemLimits.NOTE) {
            return WriteResult.Refused("The note has more than 500 characters.")
        }
        cache.item(itemId) ?: return WriteResult.Refused("Unknown item.")
        val outcome = Outcome(
            id = writer.newEntityId(),
            itemId = itemId,
            kind = kind,
            note = note.trim(),
            loggedAt = clock.now(), // device time: the server keeps it
        )
        writer.write(Operation.LogOutcome(outcome))
        return WriteResult.Saved(outcome)
    }

    /** One item with its slot and every outcome so far; null if unknown. */
    fun itemHistory(itemId: String): Flow<ItemHistory?> = flow {
        val item = cache.item(itemId)
        if (item == null) {
            emit(null)
            return@flow
        }
        emitAll(combine(cache.events(), cache.fixers(), cache.eventData(item.eventId)) {
            events, fixers, data ->
            val slot = data.slots.firstOrNull { it.itemId == itemId }
            ItemHistory(
                item = data.items.firstOrNull { it.id == itemId } ?: item,
                event = events.firstOrNull { it.id == item.eventId },
                slot = slot,
                fixer = slot?.let { s -> fixers.firstOrNull { it.id == s.fixerId } },
                outcomes = data.outcomes.filter { it.itemId == itemId },
            )
        })
    }.distinctUntilChanged().flowOn(compute)

    /** The same statistics as the server's, computed from the device copy. */
    fun stats(eventId: String): Flow<OutcomeStats> =
        cache.eventData(eventId)
            .map { outcomeStats(it.items, it.outcomes) }
            .distinctUntilChanged()
            .flowOn(compute)
}
