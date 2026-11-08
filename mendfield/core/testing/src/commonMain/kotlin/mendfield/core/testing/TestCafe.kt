// mendfield/core/testing/src/commonMain/kotlin/mendfield/core/testing/TestCafe.kt
package mendfield.core.testing

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import mendfield.core.data.EventRepository
import mendfield.core.data.InMemoryCafeCache
import mendfield.core.data.IntakeRepository
import mendfield.core.data.LocalWriter
import mendfield.core.data.ManualConnectivity
import mendfield.core.data.OutcomeRepository
import mendfield.core.data.SyncCoordinator
import mendfield.core.sync.InMemoryOutboxStore
import mendfield.core.sync.Outbox
import mendfield.core.sync.RetryPolicy
import mendfield.core.sync.SyncEngine

/** The whole data layer of the app over a [FakeCafeServer], wired by hand. */
class TestCafe(
    val server: FakeCafeServer = FakeCafeServer(),
    online: Boolean = true,
    /** Unconfined: flowOn work runs at once, inside virtual time. */
    compute: CoroutineDispatcher = Dispatchers.Unconfined,
) {
    val cache = InMemoryCafeCache()
    val store = InMemoryOutboxStore()
    val clock = TestClock()
    val writer = LocalWriter(cache, store, Outbox(store, clock, sequentialIds()))
    val events = EventRepository(server, cache, writer, compute)
    val intake = IntakeRepository(cache, writer, clock)
    val outcomes = OutcomeRepository(cache, writer, clock, compute)
    val engine = SyncEngine(store, server, RetryPolicy(maxAttempts = 2))
    val connectivity = ManualConnectivity(online)
    val sync = SyncCoordinator(store, engine, connectivity, events, cache)

    init {
        server.online = online
    }

    /** Both directions at once: the server and the monitor agree. */
    fun setOnline(online: Boolean) {
        server.online = online
        connectivity.set(online)
    }

    /** What the app does at start: the event list, then every board. */
    suspend fun loadEverything() {
        events.refreshEvents()
        server.events.forEach { events.refreshEvent(it.id) }
    }
}
