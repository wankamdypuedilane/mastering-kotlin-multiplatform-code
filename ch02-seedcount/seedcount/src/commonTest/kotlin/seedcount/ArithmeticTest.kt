package seedcount

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArithmeticTest {
    @Test
    fun ceilDivRoundsUp() {
        assertEquals(0, 0.ceilDiv(25))
        assertEquals(1, 1.ceilDiv(25))
        assertEquals(2, 50.ceilDiv(25))
        assertEquals(3, 51.ceilDiv(25))
        assertFailsWith<IllegalArgumentException> { 5.ceilDiv(0) }
    }

    @Test
    fun seedsAndPackets() {
        assertEquals(50, seedsToSow(plants = 40, rate(80)))
        assertEquals(23, seedsToSow(plants = 20, rate(90))) // 22.2 rounds up
        assertEquals(3, packetsFor(seeds = 80, seedsPerPacket = 30))
    }
}
