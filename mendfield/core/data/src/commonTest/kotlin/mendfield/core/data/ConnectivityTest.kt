// mendfield/core/data/src/commonTest/kotlin/mendfield/core/data/ConnectivityTest.kt
package mendfield.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class ConnectivityTest {
    @Test fun probesOftenWhileDownAndRarelyWhileUp() = runTest {
        var up = false
        var probes = 0
        val monitor = ProbingConnectivityMonitor({ probes++; up })
        monitor.launchIn(backgroundScope)
        runCurrent()
        assertFalse(monitor.online.value)
        assertEquals(1, probes)

        up = true
        advanceTimeBy(10.seconds + 1.seconds) // the retry interval
        assertTrue(monitor.online.value)
        assertEquals(2, probes)

        advanceTimeBy(29.seconds) // the normal interval is 30 s
        assertEquals(2, probes)
        advanceTimeBy(2.seconds)
        assertEquals(3, probes)
    }

    @Test fun workingOfflineWinsAndCheckNowProbesAtOnce() = runTest {
        var probes = 0
        val monitor = ProbingConnectivityMonitor({ probes++; true })
        monitor.launchIn(backgroundScope)
        runCurrent()
        assertTrue(monitor.online.value)

        monitor.workOffline(true)
        assertFalse(monitor.online.value)
        monitor.workOffline(false) // and probe again, without waiting
        runCurrent()
        assertTrue(monitor.online.value)
        assertEquals(2, probes)
    }

    @Test fun aThrowingProbeMeansOffline() = runTest {
        val monitor = ProbingConnectivityMonitor({ error("DNS failure") })
        monitor.launchIn(backgroundScope)
        runCurrent()
        assertFalse(monitor.online.value)
    }
}
