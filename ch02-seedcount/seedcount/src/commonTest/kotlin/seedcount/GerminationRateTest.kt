package seedcount

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GerminationRateTest {
    @Test
    fun acceptsOnlyOneToHundred() {
        assertEquals(80, GerminationRate.of(80)?.percent)
        assertNull(GerminationRate.of(0))
        assertNull(GerminationRate.of(101))
    }

    @Test
    fun measuresFromAGerminationTest() {
        val measured = GerminationRate.fromTest(sown = 20, sprouted = 17)
        assertEquals(85, measured?.percent)
        assertNull(GerminationRate.fromTest(sown = 20, sprouted = 0))
    }

    @Test
    fun rejectsImpossibleTests() {
        assertFailsWith<IllegalArgumentException> {
            GerminationRate.fromTest(sown = 0, sprouted = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            GerminationRate.fromTest(sown = 10, sprouted = 11)
        }
    }
}
