// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/Connectivity.kt
package mendfield.core.data

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Whether the outbox should try to sync now. */
interface ConnectivityMonitor {
    val online: StateFlow<Boolean>

    /** The organizer's switch: no sync attempts while it is on. */
    val workingOffline: StateFlow<Boolean>
    fun workOffline(enabled: Boolean)

    /** Look again now (the "Sync now" button); a no-op by default. */
    fun checkNow() {}
}

/**
 * The choice of Mendfield: one common monitor that asks the server itself
 * (GET /health) every [interval], every [retryInterval] while it does not
 * answer, and at once after [checkNow]. Operating-system callbacks
 * (ConnectivityManager, NWPathMonitor, navigator.onLine) only say that some
 * network exists, not that the café server is reachable through it; this
 * works the same on every target, needs no permission and is testable in
 * virtual time. The organizer can also choose to [workOffline].
 */
class ProbingConnectivityMonitor(
    private val probe: suspend () -> Boolean,
    private val interval: Duration = 30.seconds,
    private val retryInterval: Duration = 10.seconds,
) : ConnectivityMonitor {
    private val reachable = MutableStateFlow(false)
    private val offlineByChoice = MutableStateFlow(false)
    private val onlineState = MutableStateFlow(false)
    private val wake = Channel<Unit>(Channel.CONFLATED)

    override val online: StateFlow<Boolean> = onlineState.asStateFlow()
    override val workingOffline: StateFlow<Boolean> = offlineByChoice.asStateFlow()

    override fun workOffline(enabled: Boolean) {
        offlineByChoice.value = enabled
        publish()
        if (!enabled) checkNow()
    }

    /** Probe again now, instead of at the end of the current pause. */
    override fun checkNow() {
        wake.trySend(Unit)
    }

    fun launchIn(scope: CoroutineScope): Job = scope.launch {
        while (true) {
            if (!offlineByChoice.value) {
                reachable.value = try {
                    probe()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    false
                }
                publish()
            }
            val pause = if (reachable.value) interval else retryInterval
            withTimeoutOrNull(pause) { wake.receive() } // virtual in tests
        }
    }

    private fun publish() {
        onlineState.value = reachable.value && !offlineByChoice.value
    }
}

/** For tests and previews: the network is up when you say so. */
class ManualConnectivity(initial: Boolean = true) : ConnectivityMonitor {
    private val network = MutableStateFlow(initial)
    private val offlineByChoice = MutableStateFlow(false)
    private val state = MutableStateFlow(initial)
    override val online: StateFlow<Boolean> = state.asStateFlow()
    override val workingOffline: StateFlow<Boolean> = offlineByChoice.asStateFlow()

    fun set(online: Boolean) {
        network.value = online
        state.value = online && !offlineByChoice.value
    }

    override fun workOffline(enabled: Boolean) {
        offlineByChoice.value = enabled
        state.value = network.value && !enabled
    }
}
