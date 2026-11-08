// hearthseed/feature/catalog/src/commonMain/kotlin/hearthseed/feature/catalog/Labels.kt
package hearthseed.feature.catalog

import hearthseed.core.model.Difficulty
import hearthseed.core.model.SeedLot

private val monthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

fun monthsLabel(months: Set<Int>): String =
    months.sorted().joinToString(", ") { monthNames[it - 1] }

fun Difficulty.label(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }

/** "2024 harvest, 8 g, 70% germination": in lists and on the borrow screen. */
fun SeedLot.summary(): String =
    "$harvestYear harvest, $grams g, $germinationRate% germination"
