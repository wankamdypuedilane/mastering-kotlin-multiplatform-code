package hearthseed.labels

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualContainerTest {
    @Test
    fun manualRootPrintsDemoLabels() {
        val container = ManualContainer()
        assertEquals(
            "Larkmoor Commons | Moon and Stars watermelon | 2025 | 12 g" +
                " | lot L-042",
            container.printer.labelFor("L-042"),
        )
        val rows = container.sheet(columns = 2).rows()
        assertEquals(listOf(2, 1), rows.map { it.size })
    }

    @Test
    fun constructorInjectionNeedsNoFramework() {
        val pea = SeedPacket("T-1", "Test pea", 2026, 5)
        val fake = InMemoryPacketRepository(listOf(pea))
        val printer = LabelPrinter(fake) { packet -> packet.lotId }
        assertEquals(listOf("T-1"), printer.allLabels())
    }
}
