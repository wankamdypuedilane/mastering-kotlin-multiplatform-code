package language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ControlFlowTest {
    @Test
    fun ifAndWhenAreExpressions() {
        assertEquals("medium", sizeLabel(40))
        assertEquals("spring", season(4))
        assertEquals("winter", season(1))
        assertFailsWith<IllegalArgumentException> { season(13) }
        assertEquals("sow a little thicker", advice(70, 1))
        assertEquals("run a germination test first", advice(70, 4))
    }

    @Test
    fun rangesAndLoops() {
        assertEquals(
            listOf("1, 2, 3, 4, 5", "1, 2, 3, 4", "10, 5, 0", "abcde"),
            ranges(),
        )
        assertEquals(2, firstFullTray(listOf(10, 20, 24, 24), capacity = 24))
        assertEquals(-1, firstFullTray(listOf(1), capacity = 24))
    }

    @Test
    fun labeledAndNonLocalBreak() {
        val grid = listOf(listOf(3, 3), listOf(3, 3, 0, 0))
        assertEquals(1 to 2, firstEmptyCell(grid))
        assertNull(firstEmptyCell(listOf(listOf(1))))
        val batches = listOf(listOf(10, 20), listOf(15, 5))
        assertEquals(30, sownUntilFull(batches, capacity = 40))
    }
}
