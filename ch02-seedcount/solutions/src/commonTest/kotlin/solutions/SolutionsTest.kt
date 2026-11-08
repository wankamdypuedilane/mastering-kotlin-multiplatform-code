package solutions

import seedcount.GerminationRate
import seedcount.PlanResult
import seedcount.PlantingRow
import seedcount.Planner
import seedcount.SeedStock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

private fun rate(percent: Int) = GerminationRate.of(percent)!!

class SolutionsTest {
    @Test
    fun exercise1Margin() {
        assertEquals(55, seedsWithMargin(40, rate(80))) // 50 + 5
        assertEquals(50, seedsWithMargin(40, rate(80), marginPercent = 0))
        // 23 seeds plus 10 % rounded up (3)
        assertEquals(26, seedsWithMargin(20, rate(90), marginPercent = 10))
        assertFailsWith<IllegalArgumentException> {
            seedsWithMargin(1, rate(50), -1)
        }
    }

    @Test
    fun exercise2ShoppingList() {
        val planner = Planner(
            listOf(
                SeedStock("Gold Drop", 0, 20, rate(80)),
                SeedStock("Scarlet Runner", 1, 30, rate(75)),
                SeedStock("Winter Kale", 3, 100, rate(90)),
            ),
        )
        val ready = planner.plan(
            listOf(
                PlantingRow("Gold Drop", 16),
                PlantingRow("Scarlet Runner", 60),
                PlantingRow("Winter Kale", 20),
            ),
        ) as PlanResult.Ready
        assertEquals(
            mapOf("Scarlet Runner" to 2, "Gold Drop" to 1),
            ready.shoppingList(),
        )
    }

    @Test
    fun exercise3ParseStockLine() {
        assertEquals(
            StockLine.Parsed(SeedStock("Winter Kale", 3, 100, rate(90))),
            parseStockLine(" Winter Kale ; 3;100; 90"),
        )
        assertEquals(
            StockLine.Invalid(StockLineError.WrongFieldCount(3)),
            parseStockLine("Kale;3;100"),
        )
        assertEquals(
            StockLine.Invalid(
                StockLineError.NotANumber("seedsPerPacket", "many"),
            ),
            parseStockLine("Kale;3;many;90"),
        )
        assertEquals(
            StockLine.Invalid(StockLineError.OutOfRange("rate", 0)),
            parseStockLine("Kale;3;100;0"),
        )
        assertEquals(
            StockLine.Invalid(StockLineError.OutOfRange("packets", -1)),
            parseStockLine("Kale;-1;100;90"),
        )
    }

    @Test
    fun exercise4OutcomeOperators() {
        val ok: Outcome<String, Int> = Outcome.Success(2)
        val bad: Outcome<String, Int> = Outcome.Failure("no")
        assertEquals(Outcome.Success(4), ok.map { it * 2 })
        assertEquals(Outcome.Failure("no"), bad.map { it * 2 })
        assertEquals(Outcome.Failure(2), bad.mapError { it.length })
        assertEquals(
            Outcome.Failure("odd"),
            ok.flatMap { Outcome.Failure("odd") },
        )
    }

    @Test
    fun exercise4Pipeline() {
        val inventory = """
            # name;packets;seedsPerPacket;rate
            Scarlet Runner;1;30;75

            Winter Kale;3;100;90
        """.trimIndent()
        val rows = listOf(PlantingRow("Scarlet Runner", 60))
        val result = planFromText(inventory, rows)
        val ready = assertIs<Outcome.Success<PlanResult.Ready>>(result).value
        assertEquals(2, ready.totalShortfall)

        val broken = "Kale;3;100\nLeek;x;50;80\nPea;1;40;85"
        assertEquals(
            Outcome.Failure(
                listOf(
                    LineError(1, StockLineError.WrongFieldCount(3)),
                    LineError(2, StockLineError.NotANumber("packets", "x")),
                ),
            ),
            parseInventory(broken),
        )
        assertEquals(
            Outcome.Failure(listOf("No packets of 'Leek' in stock")),
            planFromText("Pea;1;40;85", listOf(PlantingRow("Leek", 3))),
        )
        val messages = planFromText(broken, emptyList())
        assertEquals(
            "line 1: WrongFieldCount(found=3)",
            (messages as Outcome.Failure).error.first(),
        )
    }
}
