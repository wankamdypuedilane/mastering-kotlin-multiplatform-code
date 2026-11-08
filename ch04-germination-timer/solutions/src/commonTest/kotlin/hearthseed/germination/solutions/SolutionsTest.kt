// ch04-germination-timer/solutions/src/commonTest/.../SolutionsTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)  // runCurrent, currentTime...

package hearthseed.germination.solutions

import hearthseed.germination.GerminationReading
import hearthseed.germination.simulatedSensor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest

class SolutionsTest {
    @Test
    fun untilFullyGerminatedStopsTheSensor() = runTest {
        val readings = simulatedSensor("A", sown = 10, listOf(3, 10, 10, 10))
            .untilFullyGerminated()
            .map { it.sprouted }
            .toList()
        assertEquals(listOf(3, 10), readings)
        assertEquals(2.hours.inWholeMilliseconds, currentTime)
    }

    @Test
    fun readAllWithinReportsSlowTraysAsNull() = runTest {
        val result = readAllWithin(
            limit = 3.seconds,
            sensors = mapOf(
                "A" to { delay(1.seconds); 4 },
                "B" to { delay(5.seconds); 7 },
                "C" to { delay(2.seconds); 9 },
            ),
        )
        assertEquals(mapOf("A" to 4, "B" to null, "C" to 9), result)
        assertEquals(3_000, currentTime)
    }

    @Test
    fun stallAlertsFireAfterSilence() = runTest {
        val readings = flow {
            delay(1.hours); emit(GerminationReading("A", 10, 1))
            delay(1.hours); emit(GerminationReading("A", 10, 2))
            delay(5.hours); emit(GerminationReading("A", 10, 4))
        }
        val alerts = readings.stallAlerts(3.hours)
            .map { id -> id to currentTime.milliseconds.inWholeHours }
            .toList()
        // Silence from 2 h to 7 h -> alert at 5 h; last reading -> 10 h.
        assertEquals(listOf("A" to 5L, "A" to 10L), alerts)
    }

    @Test
    fun regularReadingsOnlyAlertAtTheEnd() = runTest {
        val alerts = simulatedSensor("A", 10, listOf(1, 2, 3, 4))
            .stallAlerts(3.hours)
            .map { currentTime }
            .toList()
        // Readings every hour: only the final silence raises one alert.
        assertEquals(listOf(7.hours.inWholeMilliseconds), alerts)
    }
}
