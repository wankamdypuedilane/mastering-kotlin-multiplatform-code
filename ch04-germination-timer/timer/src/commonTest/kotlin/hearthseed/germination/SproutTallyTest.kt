// ch04-germination-timer/timer/src/commonTest/.../SproutTallyTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)  // currentTime

package hearthseed.germination

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class SproutTallyTest {
    private val confirm: suspend () -> Unit = { delay(10.milliseconds) }

    @Test
    fun unsafeAddLosesUpdatesEvenOnOneThread() = runTest {
        val tally = SproutTally()
        List(10) { launch { tally.addUnsafe(1, confirm) } }.joinAll()
        assertEquals(1, tally.total)          // nine of ten updates lost
    }

    @Test
    fun mutexKeepsEveryUpdate() = runTest {
        val tally = SproutTally()
        List(10) { launch { tally.add(1, confirm) } }.joinAll()
        assertEquals(10, tally.total)
        assertEquals(100, currentTime)        // the lock serializes the 10 ms
    }

    @Test
    fun updateIsAtomicAcrossThreads() = runTest {
        val tally = SproutTally()
        withContext(Dispatchers.Default) {    // real threads on JVM and iOS
            repeat(1_000) { i -> launch { tally.record("tray-${i % 4}", 1) } }
        }
        assertEquals(List(4) { 250 }, tally.perTray.value.values.toList())
    }
}
