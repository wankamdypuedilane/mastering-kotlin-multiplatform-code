// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/LocalWriter.kt
package mendfield.core.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mendfield.core.sync.Operation
import mendfield.core.sync.Outbox
import mendfield.core.sync.OutboxStore
import mendfield.core.sync.PendingOperation

/**
 * The one door to the device copy: local changes and server refreshes take
 * turns, so a refresh can never overwrite a change made while it was loading.
 */
class LocalWriter(
    private val cache: CafeCache,
    private val store: OutboxStore,
    private val transactions: TransactionRunner = SequentialWrites,
    private val outbox: Outbox = Outbox(store),
) {
    private val mutex = Mutex()

    fun newEntityId(): String = outbox.newEntityId()

    /**
     * The outbox, then the cache, in one transaction where the stores can
     * share one (Room). Where they cannot (the web), the order still
     * matters: if the app dies in between, the operation is queued and the
     * next refresh replays it into the cache ([withPending]); the other
     * order could lose the change.
     */
    suspend fun write(operation: Operation) = mutex.withLock {
        transactions.inTransaction {
            outbox.enqueue(operation)
            cache.apply(operation)
        }
    }

    /** The queue now: a refresh reads it before it asks the server. */
    suspend fun pendingSnapshot(): List<PendingOperation> = store.pending.first()

    /**
     * Stores the server's copy of an event, rebased on the unsent changes:
     * those queued now, and those that were queued when the refresh asked
     * ([pendingBefore]). The engine may deliver an operation while the
     * refresh is loading; if the server answered the read before it applied
     * that operation, only [pendingBefore] still knows it. Replaying an
     * operation that the copy already contains changes nothing.
     */
    suspend fun replaceEventData(
        eventId: String,
        server: EventData,
        pendingBefore: List<PendingOperation> = emptyList(),
    ) = mutex.withLock {
        val now = store.pending.first()
        val refused = store.rejected.first().map { it.pending.id }.toSet()
        val queuedNow = now.map { it.id }.toSet()
        val delivered = pendingBefore.filter { it.id !in queuedNow }
        val operations = (delivered + now)
            .filter { it.id !in refused }
            .map { it.operation }
        cache.replaceEventData(eventId, server.withPending(eventId, operations))
    }
}
