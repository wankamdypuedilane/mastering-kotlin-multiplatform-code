// hearthseed/core/testing/src/commonMain/kotlin/hearthseed/core/testing/SampleData.kt
package hearthseed.core.testing

import hearthseed.core.model.Difficulty
import hearthseed.core.model.LotStatus
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety

/** A small fictional catalog: 25 varieties, so that paging has 2 pages of 20. */
object SampleData {
    private val families = listOf("Solanaceae", "Fabaceae", "Brassicaceae")

    val varieties: List<Variety> = (1..25).map { n ->
        val id = "var-" + n.toString().padStart(3, '0')
        Variety(
            id = id,
            commonName = if (n == 10) "Larkmoor Pea" else "Sample variety $n",
            latinName = "Planta exempli $n",
            family = if (n == 10) "Fabaceae" else families[n % families.size],
            daysToMaturity = 40 + n,
            sowingMonths = setOf(3 + n % 4, 4 + n % 4),
            difficulty = Difficulty.entries[n % 3],
            description = "Fictional test variety number $n.",
        )
    }

    /** var-010 has one available and one borrowed lot; others have none. */
    val lots: Map<String, List<SeedLot>> = mapOf(
        "var-010" to listOf(
            SeedLot("lot-019", "var-010", 2024, 8, 70, null, LotStatus.AVAILABLE),
            SeedLot("lot-020", "var-010", 2023, 50, 88, "mem-004", LotStatus.ON_LOAN),
        ),
    )
}
