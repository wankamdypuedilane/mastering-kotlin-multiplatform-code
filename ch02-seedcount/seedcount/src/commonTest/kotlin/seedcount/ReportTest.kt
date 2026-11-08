package seedcount

import kotlin.test.Test
import kotlin.test.assertEquals

class ReportTest {
    private val planner = Planner(sampleStock)

    @Test
    fun rendersAReadyPlan() {
        val text = planner.plan(
            listOf(
                PlantingRow("Scarlet Runner", 60),
                PlantingRow("Winter Kale", 20),
            ),
        ).render()
        assertEquals(
            """
            Scarlet Runner: sow 80 for 60 plants, 3/1 packets, buy 2
            Winter Kale: sow 23 for 20 plants, 1/3 packets, ok
            """.trimIndent(),
            text,
        )
    }

    @Test
    fun rendersErrors() {
        val text = planner.plan(listOf(PlantingRow("Gold Drop", 10))).render()
        assertEquals("No packets of 'Gold Drop' in stock", text)
    }
}
