package seedpackets.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeedPacketTest {
    private val packets = SampleCatalog.packets

    @Test
    fun nullFamilyKeepsEveryPacket() {
        assertEquals(packets, packets.inFamily(null))
    }

    @Test
    fun familyFilterKeepsOnlyThatFamily() {
        val legumes = packets.inFamily(Family.LEGUMES).map { it.id }
        assertEquals(listOf("bean-runner", "pea-ladder"), legumes)
    }

    @Test
    fun emptyStockMeansUnavailable() {
        assertFalse(packets.first { it.id == "squash-moon" }.isAvailable)
        assertTrue(packets.first { it.id == "kale-ash" }.isAvailable)
    }

    @Test
    fun invalidPacketsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            SeedPacket("x", "X", Family.GREENS, daysToMaturity = 0, 1)
        }
    }
}
