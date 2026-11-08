package hearthseed.data.fakes

import hearthseed.data.remote.SeedLotDto
import hearthseed.data.remote.VarietyDto

/** A few fictional catalog entries of the Larkmoor Commons Seed Library. */
object SampleData {
    val kale = VarietyDto(
        id = "v-kale-01", commonName = "Larkmoor Winter Kale",
        latinName = "Brassica oleracea", family = "Brassicaceae",
        daysToMaturity = 55, sowingMonths = listOf(3, 4, 7, 8),
        difficulty = "EASY", description = "Hardy kale that overwinters.",
    )
    val tomato = VarietyDto(
        id = "v-tom-07", commonName = "Harbour Gold Tomato",
        latinName = "Solanum lycopersicum", family = "Solanaceae",
        daysToMaturity = 75, sowingMonths = listOf(2, 3),
        difficulty = "MEDIUM", description = "Golden cherry tomato.",
    )
    val bean = VarietyDto(
        id = "v-bean-03", commonName = "Commons Runner Bean",
        latinName = "Phaseolus coccineus", family = "Fabaceae",
        daysToMaturity = 70, sowingMonths = listOf(4, 5, 6),
        difficulty = "EASY", description = "Red-flowered climbing bean.",
    )
    val varieties = listOf(kale, tomato, bean)

    val lots = mapOf(
        "v-kale-01" to listOf(
            SeedLotDto("L-101", "v-kale-01", 2025, 40, 91, "m-12", "AVAILABLE"),
            SeedLotDto("L-102", "v-kale-01", 2024, 15, 78, null, "ON_LOAN"),
        ),
        "v-tom-07" to listOf(
            SeedLotDto("L-201", "v-tom-07", 2025, 8, 88, null, "AVAILABLE"),
            SeedLotDto("L-202", "v-tom-07", 2022, 5, 41, "m-03", "RETIRED"),
        ),
        "v-bean-03" to emptyList(),
    )
}
