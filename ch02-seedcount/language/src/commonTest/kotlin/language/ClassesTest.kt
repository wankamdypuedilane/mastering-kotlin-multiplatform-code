package language

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ClassesTest {
    @Test
    fun propertiesAndInit() {
        val tray = SeedTray("A", cells = 24)
        tray.sow(30)
        assertEquals(24, tray.sown)
        assertEquals(0, tray.free)
        assertFailsWith<IllegalArgumentException> { SeedTray("B", 0) }
    }

    @Test
    fun explicitBackingField() {
        val log = SowingLog()
        log.record("kale, tray A")
        assertEquals(listOf("kale, tray A"), log.entries)
    }

    @Test
    fun dataClasses() {
        val kale = Variety("Winter Kale", 55)
        assertEquals(Variety("Winter Kale", 55), kale)
        assertEquals(
            "Variety(name=Winter Kale, daysToMaturity=55, family=unknown)",
            kale.toString(),
        )
        val later = kale.copy(daysToMaturity = 60)
        assertNotEquals(kale, later)
        val (name, days) = later
        assertEquals("Winter Kale" to 60, name to days)
    }

    @Test
    fun enums() {
        val names = Difficulty.entries.map { it.name }
        assertEquals(listOf("EASY", "MEDIUM", "HARD"), names)
        assertEquals("***", Difficulty.valueOf("HARD").label())
        assertEquals(1, Difficulty.MEDIUM.ordinal)
    }

    @Test
    fun sealedWithGuards() {
        assertEquals(
            "start very early, under lights",
            instructions(Sowing.Indoors(10)),
        )
        assertEquals(
            "start indoors 6 weeks early",
            instructions(Sowing.Indoors(6)),
        )
        assertEquals("sow outside, 20 mm deep", instructions(Sowing.Direct(20)))
        assertEquals("not this season", instructions(Sowing.Skip))
        assertEquals("Skip", Sowing.Skip.toString())
    }

    @Test
    fun objectsAndCompanions() {
        assertEquals(10, FrostDates.sowingWeek(6))
        assertEquals(25, Packet.standard().seeds)
        assertNull(Packet.of(0))
    }

    @Test
    fun valueClasses() {
        assertEquals(Grams(15), Grams(10) + Grams(5))
        assertFailsWith<IllegalArgumentException> { Grams(-1) }
    }
}
