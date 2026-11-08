// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/LocalWriter.kt
package mendfield.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mendfield.core.sync.Operation
import mendfield.core.sync.Outbox
import mendfield.core.sync.OutboxStore

/**
 * The one door to the device copy: local changes and server refreshes take
 * turns, so a refresh can never overwrite a change made while it was loading.
 */
class LocalWriter(
    private val cache: CafeCache,
    private val store: OutboxStore,
    private val outbox: Outbox = Outbox(store),
) {
    private val mutex = Mutex()

    fun newEntityId(): String = outbox.newEntityId()

    /**
     * The outbox first, then the cache. If the app dies in between, the
     * operation is still queued and the next refresh replays it into the
     * cache ([withPending]); the other order could lose the change.
     */
    suspend fun write(operation: Operation) = mutex.withLock {
        outbox.enqueue(operation)
        cache.apply(operation)
    }

    /** Stores the server's copy of an event, rebased on the unsent changes. */
    suspend fun replaceEventData(eventId: String, server: EventData) =
        mutex.withLock {
            val pending = store.pending.first().map { it.operation }
            cache.replaceEventData(eventId,
                server.withPending(eventId, pending))
        }
}
