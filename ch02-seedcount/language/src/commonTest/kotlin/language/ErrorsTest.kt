package language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ErrorsTest {
    @Test
    fun requireAndCheck() {
        assertEquals(80, germinationPercent(sown = 50, sprouted = 40))
        val e = assertFailsWith<IllegalArgumentException> {
            germinationPercent(0, 0)
        }
        assertEquals("sown must be positive, was 0", e.message)
        val drawer = SeedDrawer()
        assertFailsWith<IllegalStateException> { drawer.take() }
        drawer.open()
        assertEquals("packet", drawer.take())
    }

    @Test
    fun tryIsAnExpression() {
        assertEquals(12, packetsFrom(" 12 "))
        assertEquals(0, packetsFrom("twelve"))
    }

    @Test
    fun resultType() {
        assertEquals(8, safePercent(50, 40).map { it / 10 }.getOrThrow())
        val failed = safePercent(0, 0)
        assertTrue(failed.isFailure)
        assertEquals(-1, failed.getOrElse { -1 })
        assertIs<IllegalArgumentException>(failed.exceptionOrNull())
    }

    @Test
    fun sealedErrors() {
        assertEquals(CountInput.Valid(3), readCount(" 3"))
        assertEquals(CountInput.Blank, readCount("  "))
        assertEquals("'three' is not a number", feedback(readCount("three")))
        assertEquals("Counts cannot be negative", feedback(readCount("-2")))
    }
}
