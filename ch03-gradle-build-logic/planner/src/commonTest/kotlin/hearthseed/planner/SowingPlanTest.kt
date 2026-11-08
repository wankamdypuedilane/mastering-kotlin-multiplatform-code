// ch03-gradle-build-logic/planner/src/commonTest/.../SowingPlanTest.kt
package hearthseed.planner

import hearthseed.units.grams
import kotlin.test.Test
import kotlin.test.assertEquals

class SowingPlanTest {
    @Test
    fun accountsForGermination() {
        // 30 plants at 80 % germination -> 38 seeds (37.5 rounded up).
        val plan = planSowing(30, germinationPercent = 80, 3.grams)
        assertEquals(38, plan.seedsToSow)
        assertEquals(1.grams, plan.seedWeight)
    }

    @Test
    fun perfectGerminationNeedsNoExtraSeeds() {
        assertEquals(12, planSowing(12, 100, 3.grams).seedsToSow)
    }
}
