// mendfield/shared/src/commonTest/kotlin/mendfield/shared/TodayFeedTest.kt
package mendfield.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mendfield.core.testing.SampleData
import mendfield.core.testing.TestCafe
import mendfield.core.testing.TestClock
import mendfield.shared.today.TodayFeed
import mendfield.shared.today.TodaySummary
import mendfield.shared.today.pickTodayEvent

@OptIn(ExperimentalCoroutinesApi::class)
class TodayFeedTest {
    @Test fun theNextEventThatHasNotEndedIsToday() {
        val events = SampleData.events
        val clock = TestClock(Instant.parse("2026-11-08T12:00:00Z"))
        assertEquals(SampleData.november, pickTodayEvent(events, clock))
        clock.now = Instant.parse("2026-11-21T18:00:00Z") // the same day
        assertEquals(SampleData.november, pickTodayEvent(events, clock))
        clock.now = Instant.parse("2026-12-01T00:00:00Z") // all past
        assertEquals(SampleData.november, pickTodayEvent(events, clock))
        assertEquals(null, pickTodayEvent(emptyList(), clock))
    }

    @Test fun swiftGetsPlainValuesThroughACallback() = runTest {
        val main = StandardTestDispatcher(testScheduler)
        val cafe = TestCafe()
        cafe.loadEverything()
        val feed = TodayFeed(cafe.events, cafe.outcomes, cafe.sync,
            TestClock(Instant.parse("2026-11-08T12:00:00Z")), main)
        val seen = mutableListOf<TodaySummary?>()
        val subscription = feed.watch { seen += it }
        advanceUntilIdle()
        assertEquals(TodaySummary("November Repair Café", "Sat 21 Nov 2026, 10:00–12:00",
            registered = 2, waiting = 1, fixed = 0, repairRate = "–",
            pendingChanges = 0, online = true), seen.last())
        subscription.cancel()
        feed.close()
    }
}
