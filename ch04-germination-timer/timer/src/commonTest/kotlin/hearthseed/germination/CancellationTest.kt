// ch04-germination-timer/timer/src/commonTest/.../CancellationTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)  // runCurrent, currentTime...

package hearthseed.germination

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest

class CancellationTest {
    @Test
    fun cancelStopsPollingAndRunsFinally() = runTest {
        var polls = 0
        var closed = false
        val job = launch {
            pollSensor(1.hours, poll = { polls++ }, close = { closed = true })
        }
        advanceTimeBy(3.hours + 30.minutes)
        job.cancel()
        job.join()
        assertEquals(3, polls)
        assertTrue(closed)
    }

    @Test
    fun timeoutReturnsNull() = runTest {
        val value = readOrNull(2.seconds) { delay(10.seconds); 7 }
        assertNull(value)
        assertEquals(2_000, currentTime)
        assertEquals(7, readOrNull(2.seconds) { delay(1.seconds); 7 })
    }

    @Test
    fun failureCancelsSiblingsInCoroutineScope() = runTest {
        var trayCFinished = false
        val sensors = mapOf<String, TrayRead>(
            "A" to { delay(1.seconds); 4 },
            "B" to { delay(2.seconds); error("lens fogged") },
            "C" to { delay(3.seconds); trayCFinished = true; 9 },
        )
        val failure = assertFailsWith<IllegalStateException> {
            readAllTraysStrict(sensors)
        }
        assertEquals("lens fogged", failure.message)
        assertEquals(false, trayCFinished)   // C was cancelled
        assertEquals(2_000, currentTime)
    }

    @Test
    fun supervisorScopeKeepsSiblingsAlive() = runTest {
        val results = readAllTrays(
            mapOf(
                "A" to { delay(1.seconds); 4 },
                "B" to { delay(2.seconds); error("lens fogged") },
                "C" to { delay(3.seconds); 9 },
            ),
        )
        assertEquals(4, results.getValue("A").getOrNull())
        assertEquals(
            "lens fogged",
            results.getValue("B").exceptionOrNull()?.message,
        )
        assertEquals(9, results.getValue("C").getOrNull())
        assertEquals(3_000, currentTime)
    }
}
