// hearthseed/solutions/src/commonMain/kotlin/hearthseed/solutions/BestLot.kt
package hearthseed.solutions

import hearthseed.core.model.SeedLot

/**
 * Exercise 11.2: the lot to offer first. Only borrowable lots count; then the
 * best germination, the freshest harvest, the most seed.
 */
fun List<SeedLot>.bestToBorrow(): SeedLot? =
    filter { it.isBorrowable }.maxWithOrNull(
        compareBy({ it.germinationRate }, { it.harvestYear }, { it.grams }),
    )
