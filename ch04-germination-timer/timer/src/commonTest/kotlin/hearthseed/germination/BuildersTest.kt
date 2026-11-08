// ch04-germination-timer/timer/src/commonTest/.../BuildersTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)  // runCurrent, currentTime...

package hearthseed.germination

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class BuildersTest {
    @Test
    fun coroutineScopeWaitsForItsChildren() = runTest {
        val log = mutableListOf<String>()
        checkTrays(log)
        log += "all checks done"
        assertEquals(
            listOf(
                "checks started", "tray A checked",
                "tray B checked", "all checks done",
            ),
            log,
        )
        assertEquals(2_000, currentTime)   // virtual milliseconds
    }

    @Test
    fun launchedWorkWaitsForTheScheduler() = runTest {
        val log = mutableListOf<String>()
        launch { checkTrays(log) }        // queued, not started yet
        assertEquals(emptyList(), log)
        runCurrent()                      // runs until the first delays
        assertEquals(listOf("checks started"), log)
        advanceUntilIdle()                // skips both delays
        assertEquals(3, log.size)
        assertEquals(2_000, currentTime)
    }

    @Test
    fun asyncRunsReadsConcurrently() = runTest {
        val total = sproutedInBothTrays(
            readA = { delay(3.seconds); 12 },
            readB = { delay(5.seconds); 30 },
        )
        assertEquals(42, total)
        assertEquals(5_000, currentTime)   // max(3 s, 5 s), not 8 s
    }
}
