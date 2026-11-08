// mendfield/core/sync/src/commonMain/kotlin/mendfield/core/sync/RetryPolicy.kt
package mendfield.core.sync

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** Exponential backoff for transient failures: 1 s, 2 s, 4 s... [maxDelay]. */
data class RetryPolicy(
    val maxAttempts: Int = 5,
    val initialDelay: Duration = 1.seconds,
    val maxDelay: Duration = 1.minutes,
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts must be at least 1" }
        require(initialDelay.isPositive() && maxDelay >= initialDelay)
    }

    /** The pause after [failedAttempts] failed attempts (1, 2, 3...). */
    fun delayAfter(failedAttempts: Int): Duration {
        var delay = initialDelay
        repeat(failedAttempts - 1) {
            // Capped at every step, so doubling never overflows.
            delay = (delay * 2).coerceAtMost(maxDelay)
        }
        return delay
    }
}
