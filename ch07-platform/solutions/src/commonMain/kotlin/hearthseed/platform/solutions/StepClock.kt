package hearthseed.platform.solutions

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** Exercise 7.1: a test clock that moves forward by [step] on every call. */
class StepClock(start: Instant, private val step: Duration) : Clock {
    private var next = start

    override fun now(): Instant = next.also { next += step }
}
