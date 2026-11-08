// mendfield/core/domain/src/jvmTest/kotlin/mendfield/core/domain/DaylightSavingTest.kt
package mendfield.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import mendfield.core.model.Event

/**
 * Region time zones, on the JVM (and on iOS) where the time-zone database
 * is built in. On JS and Wasm, TimeZone.of("Europe/London") needs the
 * @js-joda/timezone library to be loaded, so these tests stay here.
 */
class DaylightSavingTest {
    private val london = TimeZone.of("Europe/London")

    private fun event(date: LocalDate, opens: LocalTime, closes: LocalTime) =
        Event("e", "Repair café", "Hall", date, opens, closes, london)

    @Test fun theSameWallClockHoursAreDifferentInstantsInSummerAndWinter() {
        val october = event(LocalDate(2026, 10, 17), LocalTime(10, 0), LocalTime(13, 0))
        val november = event(LocalDate(2026, 11, 21), LocalTime(10, 0), LocalTime(13, 0))
        assertEquals(Instant.parse("2026-10-17T09:00:00Z"), october.hours().start) // BST
        assertEquals(Instant.parse("2026-11-21T10:00:00Z"), november.hours().start) // GMT
    }

    @Test fun anEventAcrossTheSpringChangeIsOneHourShorter() {
        // 29 March 2026: clocks jump from 01:00 to 02:00 in London.
        val night = event(LocalDate(2026, 3, 29), LocalTime(0, 30), LocalTime(3, 30))
        assertEquals(2.hours, night.hours().duration)
        assertEquals(4, slotGrid(night, "fx-1", 30.minutes).size) // not 6
    }

    @Test fun anEventAcrossTheAutumnChangeIsOneHourLonger() {
        // 25 October 2026: clocks go back from 02:00 to 01:00 in London.
        val night = event(LocalDate(2026, 10, 25), LocalTime(0, 30), LocalTime(3, 30))
        assertEquals(4.hours, night.hours().duration)
        assertEquals(8, slotGrid(night, "fx-1", 30.minutes).size)
    }
}
