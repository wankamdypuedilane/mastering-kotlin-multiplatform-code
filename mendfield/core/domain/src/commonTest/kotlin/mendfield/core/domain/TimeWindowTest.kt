// mendfield/core/domain/src/commonTest/kotlin/mendfield/core/domain/TimeWindowTest.kt
package mendfield.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.datetime.LocalTime
import mendfield.core.domain.Fixtures.at
import mendfield.core.domain.Fixtures.event

class TimeWindowTest {
    private fun w(fromHour: Int, fromMin: Int, toHour: Int, toMin: Int) =
        TimeWindow(at(fromHour, fromMin), at(toHour, toMin))

    @Test fun touchingWindowsDoNotOverlap() {
        assertFalse(w(10, 0, 10, 30).overlaps(w(10, 30, 11, 0)))
        assertFalse(w(10, 30, 11, 0).overlaps(w(10, 0, 10, 30)))
    }

    @Test fun overlapIsSymmetric() {
        assertTrue(w(10, 0, 10, 45).overlaps(w(10, 30, 11, 0)))
        assertTrue(w(10, 30, 11, 0).overlaps(w(10, 0, 10, 45)))
        assertTrue(w(10, 0, 12, 0).overlaps(w(10, 30, 11, 0))) // one inside the other
    }

    @Test fun containmentIncludesTheEdges() {
        assertTrue(w(10, 0, 11, 0) in w(10, 0, 11, 0))
        assertFalse(w(9, 30, 10, 30) in w(10, 0, 11, 0))
    }

    @Test fun anEmptyWindowIsRejected() {
        assertFailsWith<IllegalArgumentException> { w(10, 0, 10, 0) }
    }

    @Test fun eventHoursAreConvertedInTheEventTimeZone() {
        val hours = event.hours() // 10:00-13:00 at UTC+01:00
        assertEquals(Instant.parse("2026-10-17T09:00:00Z"), hours.start)
        assertEquals(Instant.parse("2026-10-17T12:00:00Z"), hours.end)
        assertEquals(3.hours, hours.duration)
    }

    @Test fun aWindowIsClippedToTheEventHours() {
        val window = event.window(LocalTime(8, 0), LocalTime(11, 0))
        assertEquals(w(10, 0, 11, 0), window)
    }

    @Test fun aWindowOutsideTheEventIsNull() {
        assertNull(event.window(LocalTime(13, 0), LocalTime(15, 0)))
        assertNull(event.window(LocalTime(11, 0), LocalTime(11, 0)))
    }
}
