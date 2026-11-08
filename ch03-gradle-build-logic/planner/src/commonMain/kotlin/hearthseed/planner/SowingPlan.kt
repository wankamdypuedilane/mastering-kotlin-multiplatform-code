// ch03-gradle-build-logic/planner/src/commonMain/.../SowingPlan.kt
package hearthseed.planner

import hearthseed.units.Grams
import hearthseed.units.gramsForSeeds

data class SowingPlan(val seedsToSow: Int, val seedWeight: Grams)

/**
 * How many seeds to sow, and how much seed to borrow from the library,
 * to raise [plantsWanted] plants at the lot's germination rate.
 */
fun planSowing(
    plantsWanted: Int,
    germinationPercent: Int,
    thousandSeedWeight: Grams,
): SowingPlan {
    require(plantsWanted >= 0) { "plantsWanted must be >= 0" }
    require(germinationPercent in 1..100) { "germination must be 1..100" }
    val seeds = (plantsWanted * 100 + germinationPercent - 1) /
        germinationPercent
    return SowingPlan(seeds, gramsForSeeds(seeds, thousandSeedWeight))
}
