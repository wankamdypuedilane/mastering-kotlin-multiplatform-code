package labels.core.solutions

import kotlin.test.Test
import kotlin.test.assertEquals

class SowingWindowAcrossYearTest {
    @Test
    fun joinsDecemberAndJanuary() {
        assertEquals("Nov–Jan", sowingWindowAcrossYear(setOf(11, 12, 1)))
        assertEquals("Jun, Dec–Feb", sowingWindowAcrossYear(setOf(1, 2, 6, 12)))
        assertEquals("Mar–Apr, Oct–Jan",
            sowingWindowAcrossYear(setOf(10, 11, 12, 1, 3, 4)))
    }

    @Test
    fun otherCasesKeepTheBasicFormat() {
        assertEquals("Feb–Mar, Oct–Nov",
            sowingWindowAcrossYear(setOf(2, 3, 10, 11)))
        assertEquals("Jan, Mar", sowingWindowAcrossYear(setOf(1, 3)))
        assertEquals("all year", sowingWindowAcrossYear((1..12).toSet()))
    }
}
