// mendfield/core/ui/src/commonTest/kotlin/mendfield/core/ui/LabelsTest.kt
package mendfield.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import mendfield.core.model.ItemCategory
import mendfield.core.model.Skill

class LabelsTest {
    @Test fun timesAreTheVenuesWallClock() {
        val instant = Instant.parse("2026-10-17T09:05:00Z")
        val bst = UtcOffset(hours = 1).asTimeZone() // London in October
        assertEquals("10:05", instant.clockTime(bst))
        assertEquals("09:00", LocalTime(9, 0).hhmm())
    }

    @Test fun datesAndEnumsReadLikeWords() {
        assertEquals("Sat 21 Nov 2026", LocalDate(2026, 11, 21).label())
        assertEquals("Small appliance", ItemCategory.SMALL_APPLIANCE.label())
        assertEquals("Electronics", Skill.ELECTRONICS.label())
    }
}
