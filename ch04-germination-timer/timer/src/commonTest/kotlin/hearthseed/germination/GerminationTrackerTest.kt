// ch04-germination-timer/timer/src/commonTest/.../GerminationTrackerTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)  // runCurrent, currentTime...

package hearthseed.germination

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class GerminationTrackerTest {
    @Test
    fun ratesPerTrayFollowVirtualTime() = runTest {
        val tracker = GerminationTracker(backgroundScope)
        val a = tracker.track(simulatedSensor("A", sown = 10, listOf(2, 5, 9)))
        val b = tracker.track(simulatedSensor("B", sown = 20, listOf(4, 10)))

        advanceTimeBy(1.hours)
        runCurrent()                             // run what is due at 1 h
        assertEquals(mapOf("A" to 20, "B" to 20), tracker.rates.value)

        joinAll(a, b)                            // wait for both sensors
        assertEquals(mapOf("A" to 90, "B" to 50), tracker.rates.value)
        assertEquals(3.hours.inWholeMilliseconds, currentTime)
    }

    @Test
    fun readyEventIsSentOnceWhenThresholdIsCrossed() = runTest {
        val tracker = GerminationTracker(backgroundScope, readyAt = 80)
        val events = mutableListOf<TrayReady>()
        backgroundScope.launch { tracker.ready.toList(events) }
        runCurrent()                             // subscribe first
        tracker.track(simulatedSensor("A", sown = 10, listOf(5, 8, 9, 10)))
            .join()
        assertEquals(listOf(TrayReady("A", 80)), events)
    }

    @Test
    fun averageRateIsComputedOnlyWhileObserved() = runTest {
        val tracker = GerminationTracker(backgroundScope)
        joinAll(
            tracker.track(simulatedSensor("A", sown = 10, listOf(8))),
            tracker.track(simulatedSensor("B", sown = 10, listOf(4))),
        )
        assertEquals(mapOf("A" to 80, "B" to 40), tracker.rates.value)
        assertEquals(0, tracker.averageRate.value)   // nobody observes yet

        backgroundScope.launch { tracker.averageRate.collect {} }
        runCurrent()
        assertEquals(60, tracker.averageRate.value)  // (80 + 40) / 2
    }
}
