// mendfield/core/testing/src/commonMain/kotlin/mendfield/core/testing/TestClock.kt
package mendfield.core.testing

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** A clock the test moves by hand. */
class TestClock(var now: Instant = SampleData.at(0)) : Clock {
    override fun now(): Instant = now
    operator fun plusAssign(duration: Duration) {
        now += duration
    }
}

/**
 * Ids "id-1", "id-2"... instead of random UUIDs. A function, not a class
 * implementing () -> String: Kotlin/JS forbids implementing function types.
 */
fun sequentialIds(prefix: String = "id"): () -> String {
    var n = 0
    return { "$prefix-${++n}" }
}
