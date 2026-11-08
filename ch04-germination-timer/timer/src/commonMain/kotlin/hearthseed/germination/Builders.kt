// ch04-germination-timer/timer/src/commonMain/.../Builders.kt
package hearthseed.germination

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Starts two checks and returns only when both have finished. */
suspend fun checkTrays(log: MutableList<String>) = coroutineScope {
    launch {
        delay(2.seconds)
        log += "tray B checked"
    }
    launch {
        delay(1.seconds)
        log += "tray A checked"
    }
    log += "checks started"
}   // coroutineScope waits here for both children

/** Reads two trays concurrently: the total time is the slower read. */
suspend fun sproutedInBothTrays(
    readA: suspend () -> Int,
    readB: suspend () -> Int,
): Int = coroutineScope {
    val a = async { readA() }
    val b = async { readB() }
    a.await() + b.await()
}
