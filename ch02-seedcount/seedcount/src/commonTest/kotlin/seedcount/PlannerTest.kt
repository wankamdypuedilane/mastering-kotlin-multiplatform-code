package seedcount

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PlannerTest {
    private val planner = Planner(sampleStock)

    @Test
    fun computesPacketsAndShortfall() {
        val result = planner.plan(
            listOf(
                PlantingRow("Larkmoor Red", 40),
                PlantingRow("Scarlet Runner", 60),
                PlantingRow("Winter Kale", 20),
            ),
        )
        val ready = assertIs<PlanResult.Ready>(result)
        assertEquals(
            PlanLine(
                "Scarlet Runner", plants = 60, seedsToSow = 80,
                packetsNeeded = 3, packetsInStock = 1,
            ),
            ready.lines.first(), // largest shortfall first
        )
        assertEquals(listOf(2, 0, 0), ready.lines.map { it.shortfall })
        assertEquals(2, ready.totalShortfall)
    }

    @Test
    fun mergesRowsOfTheSameVariety() {
        val result = planner.plan(
            listOf(
                PlantingRow("winter kale", 20),
                PlantingRow("Winter Kale ", 70),
            ),
        )
        val line = assertIs<PlanResult.Ready>(result).lines.single()
        assertEquals(90, line.plants)
        assertEquals(100, line.seedsToSow)
        assertEquals(1, line.packetsNeeded)
    }

    @Test
    fun collectsEveryError() {
        val result = planner.plan(
            listOf(
                PlantingRow("Gold Drop", 10),
                PlantingRow("Winter Kale", 0),
                PlantingRow("Larkmoor Red", 5),
            ),
        )
        assertEquals(
            PlanResult.Rejected(
                listOf(
                    PlanError.UnknownVariety("Gold Drop"),
                    PlanError.InvalidPlantCount("Winter Kale", 0),
                ),
            ),
            result,
        )
    }
}
