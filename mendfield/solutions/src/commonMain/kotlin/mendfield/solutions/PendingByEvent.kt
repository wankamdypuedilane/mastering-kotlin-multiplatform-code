// mendfield/solutions/src/commonMain/kotlin/mendfield/solutions/PendingByEvent.kt
package mendfield.solutions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import mendfield.core.data.CafeCache
import mendfield.core.sync.Operation
import mendfield.core.sync.OutboxStore

/** Operations whose event this device no longer knows. */
const val UNKNOWN_EVENT = "?"

/**
 * Exercise 23.2: the number of unsent changes per event id, for a badge on
 * the event list. It re-emits only when a count changes.
 */
fun pendingByEvent(
    store: OutboxStore,
    cache: CafeCache,
): Flow<Map<String, Int>> =
    store.pending
        .map { queue ->
            queue.map { eventIdOf(it.operation, cache) }
                .groupingBy { it }.eachCount()
        }
        .distinctUntilChanged()

private suspend fun eventIdOf(op: Operation, cache: CafeCache): String =
    when (op) {
        is Operation.RegisterItem -> op.item.eventId
        is Operation.AssignSlot -> cache.slot(op.slotId)?.eventId
        is Operation.LogOutcome -> cache.item(op.outcome.itemId)?.eventId
    } ?: UNKNOWN_EVENT
