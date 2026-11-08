package demo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PacketLabelTest {
    @Test
    fun formatsVarietyAndYear() = assertEquals(
        "Larkmoor Runner - harvest 2026",
        packetLabel("Larkmoor Runner", 2026),
    )

    @Test
    fun trimsTheVarietyName() = assertEquals(
        "Moss Pea - harvest 2025",
        packetLabel("  Moss Pea ", 2025),
    )

    @Test
    fun rejectsABlankVariety() {
        assertFailsWith<IllegalArgumentException> { packetLabel(" ", 2026) }
    }
}
