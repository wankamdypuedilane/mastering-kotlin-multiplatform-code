package seedloans.solutions

import kotlinx.coroutines.delay
import seedloans.ReminderSender
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Exercise 20.3: retries a failed delivery, waiting longer each time. */
class RetryingSender(
    private val delegate: ReminderSender,
    private val attempts: Int = 3,
    private val backoff: Duration = 30.seconds,
) : ReminderSender {
    init {
        require(attempts >= 1) { "attempts must be at least 1" }
    }

    override suspend fun send(memberId: String, text: String): Boolean {
        repeat(attempts) { attempt ->
            if (delegate.send(memberId, text)) return true
            if (attempt < attempts - 1) delay(backoff * (attempt + 1))
        }
        return false
    }
}
