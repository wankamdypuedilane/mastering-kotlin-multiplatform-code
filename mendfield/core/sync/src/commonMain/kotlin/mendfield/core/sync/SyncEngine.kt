// mendfield/core/sync/src/commonMain/kotlin/mendfield/core/sync/SyncEngine.kt
package mendfield.core.sync

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class WaitingToRetry(
        val failedAttempts: Int,
        val delay: Duration,
    ) : SyncState
    data class Failed(val reason: String) : SyncState
}

sealed interface SyncReport {
    val applied: Int
    val rejected: Int

    /** The outbox is empty. */
    data class Done(
        override val applied: Int,
        override val rejected: Int,
    ) : SyncReport

    /** Still offline after every retry; the rest waits for the next trigger. */
    data class GaveUp(
        override val applied: Int,
        override val rejected: Int,
        val reason: String,
    ) : SyncReport

    /** A permanent failure (such as a bad token): retrying will not help. */
    data class Stopped(
        override val applied: Int,
        override val rejected: Int,
        val reason: String,
    ) : SyncReport
}

/**
 * Pushes the outbox to the server in batches, oldest first. Every operation
 * keeps its id across attempts, so resending a batch whose response was lost
 * is safe: the server answers a replay with the result it gave the first time.
 */
class SyncEngine(
    private val store: OutboxStore,
    private val transport: SyncTransport,
    private val policy: RetryPolicy = RetryPolicy(),
    private val batchSize: Int = 20,
) {
    init {
        require(batchSize in 1..50) { "The server accepts 1 to 50 operations" }
    }

    private val mutex = Mutex() // one sync at a time
    private val stateFlow = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = stateFlow.asStateFlow()

    suspend fun syncNow(): SyncReport = mutex.withLock {
        val report = drain()
        stateFlow.value = when (report) {
            is SyncReport.Done -> SyncState.Idle
            is SyncReport.GaveUp -> SyncState.Failed(report.reason)
            is SyncReport.Stopped -> SyncState.Failed(report.reason)
        }
        report
    }

    /**
     * Syncs whenever the device is [online] and there is work: on
     * reconnection, when an operation is added while online, and again
     * [RetryPolicy.maxDelay] after a sync that gave up while still online.
     */
    fun launchIn(scope: CoroutineScope, online: Flow<Boolean>): Job =
        scope.launch {
            val newest = store.pending.map { it.lastOrNull()?.id }
            val retries = MutableStateFlow(0) // a tick re-runs the check
            var retry: Job? = null
            combine(online, newest, retries) { isOnline, id, tick ->
                Triple(isOnline, id, tick)
            }
                .distinctUntilChanged()
                .conflate() // a burst of changes gives one more sync
                .collect { (isOnline, newestId, _) ->
                    if (!isOnline || newestId == null) return@collect
                    if (syncNow() is SyncReport.GaveUp) {
                        retry?.cancel()
                        retry = launch { // a child: cancelled with the engine
                            delay(policy.maxDelay)
                            retries.value++
                        }
                    }
                }
        }

    private suspend fun drain(): SyncReport {
        var applied = 0
        var rejected = 0
        while (true) {
            val batch = store.oldest(batchSize)
            if (batch.isEmpty()) return SyncReport.Done(applied, rejected)
            when (val result = pushWithRetry(batch)) {
                is PushResult.Transient ->
                    return SyncReport.GaveUp(applied, rejected, result.reason)
                is PushResult.Permanent ->
                    return SyncReport.Stopped(applied, rejected, result.reason)
                is PushResult.Delivered -> {
                    val byId = result.results.associateBy { it.operationId }
                    val answered = batch.filter { it.id in byId }
                    if (answered.isEmpty()) {
                        return SyncReport.Stopped(applied, rejected,
                            "No result for any of ${batch.size} operations")
                    }
                    val refusals = answered.mapNotNull { op ->
                        val r = byId.getValue(op.id)
                        if (r !is OperationResult.Rejected) null
                        else RejectedOperation(op, r.code, r.message)
                    }
                    val refusedIds = refusals.map { it.pending.id }.toSet()
                    val confirmed = answered.map { it.id }.toSet() - refusedIds
                    store.remove(confirmed)
                    store.reject(refusals)
                    applied += confirmed.size
                    rejected += refusals.size
                }
            }
        }
    }

    private suspend fun pushWithRetry(
        batch: List<PendingOperation>,
    ): PushResult {
        var attempt = 1
        while (true) {
            stateFlow.value = SyncState.Syncing
            val result = try {
                transport.push(batch)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                PushResult.Transient(e.message ?: "Transport failed")
            }
            if (result !is PushResult.Transient) return result
            if (attempt >= policy.maxAttempts) return result
            val pause = policy.delayAfter(attempt)
            stateFlow.value = SyncState.WaitingToRetry(attempt, pause)
            delay(pause) // virtual time in tests
            attempt++
        }
    }
}
