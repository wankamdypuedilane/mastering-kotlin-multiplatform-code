// ch04-germination-timer/timer/src/commonTest/.../CombiningTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)  // currentTime

package hearthseed.germination

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest

class CombiningTest {
    // Tray A reports at 1 h and 2 h; tray B at 1.5 h and 3 h.
    private val trayA = simulatedSensor("A", sown = 10, listOf(6, 7))
    private val trayB = simulatedSensor("B", 10, listOf(4, 8), 90.minutes)

    /** Hours of virtual time, to one decimal: 90 min -> 1.5. */
    private fun hoursNow(time: Long) = time.milliseconds.inWholeMinutes / 60.0

    @Test
    fun combineWaitsForBothThenFollowsEither() = runTest {
        val means = meanRate(trayA, trayB)
            .map { it to hoursNow(currentTime) }
            .toList()
        // 1 h: only A has reported, so nothing yet.
        assertEquals(listOf(50 to 1.5, 55 to 2.0, 75 to 3.0), means)
    }

    @Test
    fun zipPairsReadingsOneToOne() = runTest {
        assertEquals(listOf("A", "B"), leaderPerPhoto(trayA, trayB).toList())
        assertEquals(3.hours.inWholeMilliseconds, currentTime)
    }

    @Test
    fun mergeInterleavesInArrivalOrder() = runTest {
        val ids = allReadings(trayA, trayB).map { it.trayId }.toList()
        assertEquals(listOf("A", "B", "A", "B"), ids)
    }

    @Test
    fun flatMapLatestSwitchesToTheNewTray() = runTest {
        val selection = flow {
            emit("A")
            delay(150.minutes)          // the user switches at 2.5 h
            emit("B")
        }
        val seen = selection
            .followTray { id -> simulatedSensor(id, 10, listOf(1, 2, 3, 4)) }
            .map { "${it.trayId}${it.sprouted}" }
            .toList()
        // A's third photo (3 h) never comes; B starts again from scratch.
        assertEquals(listOf("A1", "A2", "B1", "B2", "B3", "B4"), seen)
        assertEquals(390.minutes.inWholeMilliseconds, currentTime)
    }
}
