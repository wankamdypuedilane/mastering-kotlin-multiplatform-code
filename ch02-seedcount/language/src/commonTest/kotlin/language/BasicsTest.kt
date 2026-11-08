package language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BasicsTest {
    @Test
    fun valuesAndInference() {
        assertEquals("Tomato: 5 packets, 24 g", inventoryLine())
        assertEquals(25, SEEDS_PER_PACKET)
    }

    @Test
    fun nullSafety() {
        assertEquals("GOLD DROP", labelFor("  gold drop "))
        assertEquals("UNLABELED", labelFor(null))
        assertEquals(4, lengthOrFail("Kale"))
        assertFailsWith<NullPointerException> { lengthOrFail(null) }
    }

    @Test
    fun smartCasts() {
        assertEquals(0, doubled(null))
        assertEquals(6, doubled(3))
        assertEquals("12 g", weightLabel(12))
        assertEquals("unknown", weightLabel("12"))
        assertEquals(4, sizeOf("Leek"))
        assertEquals(-1, sizeOf(42))
    }

    @Test
    fun multiDollarInterpolation() {
        val expected = "{\"\$schema\": \"seed-stock\", \"count\": 4}"
        assertEquals(expected, schemaLine(4))
    }
}
