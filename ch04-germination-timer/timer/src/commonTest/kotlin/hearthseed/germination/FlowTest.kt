// ch04-germination-timer/timer/src/commonTest/.../FlowTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)  // runCurrent, currentTime...

package hearthseed.germination

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class FlowTest {
    @Test
    fun sensorIsColdAndRestartsForEachCollector() = runTest {
        var starts = 0
        val sensor = simulatedSensor("A", sown = 10, listOf(2, 5, 9))
            .onStart { starts++ }
        assertEquals(0, starts)              // building a flow runs nothing
        val first = sensor.map { it.ratePercent }.toList()
        val second = sensor.map { it.ratePercent }.toList()
        assertEquals(listOf(20, 50, 90), first)
        assertEquals(first, second)
        assertEquals(2, starts)
        assertEquals(6.hours.inWholeMilliseconds, currentTime)
    }

    @Test
    fun milestonesSkipRepeatsAndZero() = runTest {
        val sensor = simulatedSensor(
            "A", sown = 20, sproutCounts = listOf(0, 1, 3, 4, 9, 10, 20),
        )
        assertEquals(listOf(10, 20, 40, 50, 100), sensor.milestones().toList())
    }

    @Test
    fun stateFlowCollectorSeesOnlyTheLatestValue() = runTest {
        val rate = MutableStateFlow(0)
        val seen = mutableListOf<Int>()
        backgroundScope.launch { rate.collect { seen += it } }
        runCurrent()                         // collector starts, sees 0
        rate.value = 30
        rate.value = 60
        rate.value = 90                      // no suspension in between
        runCurrent()
        assertEquals(listOf(0, 90), seen)    // 30 and 60 were conflated
    }
}
