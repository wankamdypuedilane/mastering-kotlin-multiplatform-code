// ch04-germination-timer/timer/src/commonMain/.../Cancellation.kt
package hearthseed.germination

import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/** Polls a sensor every [interval] until cancelled, then closes it. */
suspend fun pollSensor(
    interval: Duration,
    poll: () -> Unit,
    close: () -> Unit,
) {
    try {
        while (true) {
            delay(interval)   // suspension point: cancellation lands here
            poll()
        }
    } finally {
        close()               // runs on normal exit and on cancellation
    }
}

/** Reads a slow sensor, or returns null when it takes longer than [limit]. */
suspend fun readOrNull(limit: Duration, read: suspend () -> Int): Int? =
    withTimeoutOrNull(limit) { read() }
