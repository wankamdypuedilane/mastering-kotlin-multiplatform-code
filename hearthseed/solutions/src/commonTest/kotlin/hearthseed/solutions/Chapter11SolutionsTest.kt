// hearthseed/solutions/src/commonTest/kotlin/hearthseed/solutions/Chapter11SolutionsTest.kt
package hearthseed.solutions

import hearthseed.core.model.Difficulty
import hearthseed.core.model.LotStatus
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class Chapter11SolutionsTest {
    private fun variety(vararg months: Int) = Variety("var-1", "Pea", "Pisum sativum",
        "Fabaceae", 60, months.toSet(), Difficulty.EASY, "")

    @Test fun sowingWindowMergesRunsOfMonths() {
        assertEquals("Mar–Apr", variety(4, 3).sowingWindow())
        assertEquals("Feb, May–Jul", variety(2, 5, 6, 7).sowingWindow())
        assertEquals("Mar–Sep", variety(3, 4, 5, 6, 7, 8, 9).sowingWindow())
        assertEquals("Oct", variety(10).sowingWindow())
    }

    private fun lot(id: String, rate: Int, year: Int, grams: Int = 10,
                    status: LotStatus = LotStatus.AVAILABLE) =
        SeedLot(id, "var-1", year, grams, rate, null, status)

    @Test fun bestLotIsAvailableThenBestGerminationThenFreshest() {
        val lots = listOf(
            lot("a", rate = 95, year = 2024, status = LotStatus.ON_LOAN),
            lot("b", rate = 88, year = 2023),
            lot("c", rate = 88, year = 2024, grams = 5),
            lot("d", rate = 70, year = 2025),
        )
        assertEquals("c", lots.bestToBorrow()?.id)
        assertNull(listOf(lot("x", 90, 2024, status = LotStatus.RETIRED)).bestToBorrow())
    }

    @Test fun catalogQueryFollowsTheContract() {
        assertEquals(
            mapOf("query" to "pea", "family" to "Fabaceae", "page" to "2", "pageSize" to "10"),
            CatalogQuery(" pea ", "fabaceae", page = 2, pageSize = 10).toParameters(),
        )
        assertEquals(mapOf("page" to "1", "pageSize" to "20"), CatalogQuery("  ").toParameters())
        val bad = CatalogQuery(family = "Rosaceae", page = 0, pageSize = 500)
        assertEquals(3, bad.problems().size)
        assertFailsWith<IllegalArgumentException> { bad.toParameters() }
    }
}
