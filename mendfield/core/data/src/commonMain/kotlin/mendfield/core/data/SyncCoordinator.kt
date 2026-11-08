// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/SyncCoordinator.kt
package mendfield.core.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mendfield.core.sync.Operation
import mendfield.core.sync.OutboxStore
import mendfield.core.sync.RejectedOperation
import mendfield.core.sync.SyncEngine
import mendfield.core.sync.SyncReport
import mendfield.core.sync.SyncState

/** What the sync indicator shows. */
data class SyncStatus(
    val online: Boolean,
    val pending: Int,
    val rejected: List<RejectedOperation>,
    val state: SyncState,
    val workingOffline: Boolean = false,
)

/**
 * Glue between the outbox and the rest of the app: it runs the engine while
 * the server is reachable, refreshes the events whose operations the server
 * refused (so the screens show the server's truth again), and refreshes the
 * event list after every reconnection.
 */
class SyncCoordinator(
    private val store: OutboxStore,
    private val engine: SyncEngine,
    private val connectivity: ConnectivityMonitor,
    private val events: EventRepository,
    private val cache: CafeCache,
) {
    val status: Flow<SyncStatus> = combine(
        connectivity.online,
        store.pending.map { it.size }.distinctUntilChanged(),
        store.rejected,
        engine.state,
        connectivity.workingOffline,
    ) { online, pending, rejected, state, offline ->
        SyncStatus(online, pending, rejected, state, offline)
    }

    fun start(scope: CoroutineScope): Job = scope.launch {
        if (connectivity is ProbingConnectivityMonitor) connectivity.launchIn(this)
        engine.launchIn(this, connectivity.online)
        launch { refreshAfterRejections() }
        launch {
            connectivity.online.drop(1).filter { it }.collect {
                events.refreshEvents()
            }
        }
    }

    /** The "Sync now" button: never while the organizer works offline. */
    suspend fun syncNow(): SyncReport? {
        if (connectivity.workingOffline.value) return null
        connectivity.checkNow()
        return engine.syncNow()
    }

    fun workOffline(enabled: Boolean) = connectivity.workOffline(enabled)

    suspend fun dismiss(operationId: String) = store.dismiss(operationId)

    private suspend fun refreshAfterRejections() {
        val seen = mutableSetOf<String>()
        store.rejected.collect { rejected ->
            val fresh = rejected.filter { seen.add(it.pending.id) }
            fresh.mapNotNull { eventIdOf(it.pending.operation) }.toSet()
                .forEach { events.refreshEvent(it) }
        }
    }

    private suspend fun eventIdOf(op: Operation): String? = when (op) {
        is Operation.RegisterItem -> op.item.eventId
        is Operation.AssignSlot -> cache.slot(op.slotId)?.eventId
        is Operation.LogOutcome -> cache.item(op.outcome.itemId)?.eventId
    }
}
