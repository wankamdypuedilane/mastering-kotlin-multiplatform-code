// mendfield/core/sync/src/commonTest/kotlin/mendfield/core/sync/SyncEngineTest.kt
package mendfield.core.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import mendfield.core.sync.FakeServer.Step

@OptIn(ExperimentalCoroutinesApi::class)
class SyncEngineTest {
    private val store = InMemoryOutboxStore()
    private val server = FakeServer()
    private val policy = RetryPolicy(maxAttempts = 4, initialDelay = 1.seconds)

    private suspend fun queue(vararg ids: String) = ids.forEach { store.add(pending(it)) }

    @Test fun drainsTheOutboxInBatches() = runTest {
        queue("a", "b", "c", "d", "e")
        val engine = SyncEngine(store, server, policy, batchSize = 2)
        assertEquals(SyncReport.Done(applied = 5, rejected = 0), engine.syncNow())
        assertEquals(listOf(listOf("a", "b"), listOf("c", "d"), listOf("e")), server.batches)
        assertEquals(emptyList(), store.pending.value)
        assertEquals(SyncState.Idle, engine.state.value)
    }

    @Test fun refusedOperationsMoveToTheRejectedList() = runTest {
        store.add(pending("a"))
        store.add(pending("b", Operation.AssignSlot("taken", "i-b")))
        store.add(pending("c"))
        val report = SyncEngine(store, server, policy).syncNow()
        assertEquals(SyncReport.Done(applied = 2, rejected = 1), report)
        val refused = store.rejected.value.single()
        assertEquals("b" to "slot_taken", refused.pending.id to refused.code)
    }

    @Test fun transientFailuresAreRetriedWithExponentialBackoff() = runTest {
        queue("a")
        server.script += listOf(Step.OFFLINE, Step.OFFLINE)
        val engine = SyncEngine(store, server, policy)
        assertEquals(SyncReport.Done(1, 0), engine.syncNow())
        assertEquals(3_000, currentTime) // waited 1 s, then 2 s (virtual time)
        assertEquals(3, server.batches.size)
    }

    @Test fun theStateShowsTheNextRetry() = runTest {
        queue("a")
        server.script += Step.OFFLINE
        val engine = SyncEngine(store, server, policy)
        val job = launch { engine.syncNow() }
        testScheduler.advanceTimeBy(500) // inside the first pause
        assertEquals(SyncState.WaitingToRetry(failedAttempts = 1, delay = 1.seconds),
            engine.state.value)
        job.join()
        assertEquals(SyncState.Idle, engine.state.value)
    }

    @Test fun givesUpAfterTheLastAttemptAndKeepsTheOperations() = runTest {
        queue("a", "b")
        server.script += List(4) { Step.OFFLINE }
        val engine = SyncEngine(store, server, policy)
        val report = engine.syncNow()
        assertEquals(SyncReport.GaveUp(0, 0, "offline"), report)
        assertEquals(7_000, currentTime) // 1 + 2 + 4 s between four attempts
        assertEquals(listOf("a", "b"), store.pending.value.map { it.id })
        assertEquals(SyncState.Failed("offline"), engine.state.value)
    }

    @Test fun aPermanentFailureStopsWithoutRetrying() = runTest {
        queue("a")
        server.script += Step.UNAUTHORIZED
        val report = SyncEngine(store, server, policy).syncNow()
        assertEquals(SyncReport.Stopped(0, 0, "Unauthorized"), report)
        assertEquals(0, currentTime)
        assertEquals(1, store.pending.value.size)
    }

    @Test fun aLostResponseIsResentAndAppliedOnce() = runTest {
        queue("a", "b")
        server.script += Step.LOSE_RESPONSE // applied, but the device never hears
        val report = SyncEngine(store, server, policy).syncNow()
        assertEquals(SyncReport.Done(2, 0), report)
        assertEquals(listOf(listOf("a", "b"), listOf("a", "b")), server.batches)
        assertEquals(listOf("a", "b"), server.applied) // not four times
    }

    @Test fun aServerThatAnswersNothingCannotLoopForever() = runTest {
        queue("a")
        val silent = SyncTransport { PushResult.Delivered(emptyList()) }
        val report = SyncEngine(store, silent, policy).syncNow()
        assertIs<SyncReport.Stopped>(report)
        assertEquals(1, store.pending.value.size)
    }

    @Test fun aThrowingTransportCountsAsTransient() = runTest {
        queue("a")
        var calls = 0
        val flaky = SyncTransport { batch ->
            if (++calls == 1) error("socket closed")
            PushResult.Delivered(batch.map { OperationResult.Applied(it.id) })
        }
        assertEquals(SyncReport.Done(1, 0), SyncEngine(store, flaky, policy).syncNow())
        assertEquals(2, calls)
    }

    @Test fun concurrentSyncsRunOneAfterTheOther() = runTest {
        queue("a", "b", "c")
        val engine = SyncEngine(store, server, policy, batchSize = 1)
        val first = launch { engine.syncNow() }
        val second = launch { engine.syncNow() }
        first.join(); second.join()
        assertEquals(1, server.maxInFlight)
        assertEquals(listOf("a", "b", "c"), server.applied)
    }

    @Test fun syncsOnReconnectionAndOnNewWork() = runTest {
        val online = MutableStateFlow(false)
        val engine = SyncEngine(store, server, policy)
        engine.launchIn(backgroundScope, online)
        queue("a")
        runCurrent()
        assertEquals(emptyList(), server.batches) // offline: nothing sent

        online.value = true
        runCurrent()
        assertEquals(listOf(listOf("a")), server.batches)

        queue("b") // added while online
        runCurrent()
        assertEquals(listOf(listOf("a"), listOf("b")), server.batches)
        assertEquals(emptyList(), store.pending.value)
    }

    @Test fun afterGivingUpWhileOnlineItTriesAgainAfterTheMaximumDelay() = runTest {
        val online = MutableStateFlow(true)
        val engine = SyncEngine(store, server, policy) // maxDelay: 1 minute
        repeat(policy.maxAttempts) { server.script += Step.OFFLINE }
        engine.launchIn(backgroundScope, online)
        queue("a")
        advanceTimeBy(7.seconds + 1.milliseconds) // 1 + 2 + 4 s of backoff
        assertEquals(4, server.batches.size) // gave up, still online
        assertEquals(listOf("a"), store.pending.value.map { it.id })

        advanceTimeBy(1.minutes) // no new work, no reconnection: a retry
        assertEquals(5, server.batches.size)
        assertEquals(emptyList(), store.pending.value)
        advanceTimeBy(10.minutes) // the outbox is empty: no more pushes
        assertEquals(5, server.batches.size)
    }

    @Test fun noRetryWhileOffline() = runTest {
        val online = MutableStateFlow(true)
        val engine = SyncEngine(store, server, policy)
        repeat(policy.maxAttempts) { server.script += Step.OFFLINE }
        engine.launchIn(backgroundScope, online)
        queue("a")
        advanceTimeBy(8.seconds)
        online.value = false // the probe noticed
        advanceTimeBy(5.minutes)
        assertEquals(4, server.batches.size)
        online.value = true // reconnection syncs at once
        runCurrent()
        assertEquals(5, server.batches.size)
    }

    @Test fun backoffDoublesUpToTheMaximum() {
        val p = RetryPolicy(maxAttempts = 10, initialDelay = 1.seconds, maxDelay = 10.seconds)
        assertEquals(listOf(1, 2, 4, 8, 10, 10).map { it.seconds }, (1..6).map(p::delayAfter))
    }
}
