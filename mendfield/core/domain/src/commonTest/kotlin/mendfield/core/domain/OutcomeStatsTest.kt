// mendfield/core/domain/src/commonTest/kotlin/mendfield/core/domain/OutcomeStatsTest.kt
package mendfield.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import mendfield.core.domain.Fixtures.at
import mendfield.core.domain.Fixtures.item
import mendfield.core.model.ItemCategory
import mendfield.core.model.Outcome
import mendfield.core.model.OutcomeKind
import mendfield.core.model.OutcomeKind.ADVICE
import mendfield.core.model.OutcomeKind.FIXED
import mendfield.core.model.OutcomeKind.NOT_FIXED
import mendfield.core.model.OutcomeKind.PARTIAL

class OutcomeStatsTest {
    private var next = 0
    private fun outcome(itemId: String, kind: OutcomeKind, minute: Int = 0) =
        Outcome("o${next++}", itemId, kind, note = "", loggedAt = at(11) + minute.minutes)

    @Test fun countsEveryKindIncludingZeros() {
        val items = listOf(item("a", ItemCategory.LAMP), item("b", ItemCategory.TOY))
        val stats = outcomeStats(items, listOf(outcome("a", FIXED)))
        assertEquals(mapOf(FIXED to 1, PARTIAL to 0, NOT_FIXED to 0, ADVICE to 0), stats.counts)
        assertEquals(OutcomeKind.entries, stats.counts.keys.toList())
        assertEquals(2, stats.registered)
        assertEquals(1, stats.withOutcome)
        assertEquals(1, stats.pending)
    }

    @Test fun theLatestOutcomeOfAnItemWins() {
        val items = listOf(item("a", ItemCategory.LAMP))
        val outcomes = listOf(outcome("a", FIXED, minute = 30), outcome("a", NOT_FIXED, minute = 5))
        val stats = outcomeStats(items, outcomes)
        assertEquals(1, stats.counts[FIXED])
        assertEquals(0, stats.counts[NOT_FIXED])
    }

    @Test fun equalTimestampsAreBrokenById() {
        val items = listOf(item("a", ItemCategory.LAMP))
        val first = Outcome("o-1", "a", PARTIAL, "", at(11))
        val second = Outcome("o-2", "a", FIXED, "", at(11))
        assertEquals(1, outcomeStats(items, listOf(second, first)).counts[FIXED])
    }

    @Test fun outcomesOfUnknownItemsAreIgnored() {
        val stats = outcomeStats(listOf(item("a", ItemCategory.LAMP)),
            listOf(outcome("ghost", FIXED)))
        assertEquals(0, stats.withOutcome)
        assertNull(stats.repairRatePercent)
    }

    @Test fun adviceIsNotARepairAttempt() {
        val items = listOf("a", "b", "c").map { item(it, ItemCategory.CLOCK) }
        val outcomes = listOf(outcome("a", FIXED), outcome("b", NOT_FIXED), outcome("c", ADVICE))
        assertEquals(50, outcomeStats(items, outcomes).repairRatePercent) // 1 of 2
    }

    @Test fun partialRepairsCountAsRepaired() {
        val items = (1..9).map { item("i$it", ItemCategory.LAMP) }
        val kinds = List(5) { FIXED } + List(2) { PARTIAL } + List(2) { NOT_FIXED }
        val outcomes = items.zip(kinds) { item, kind -> outcome(item.id, kind) }
        assertEquals(78, outcomeStats(items, outcomes).repairRatePercent) // 7/9 = 77.8 %
    }

    @Test fun ratesAreRoundedHalfUp() {
        val items = (1..8).map { item("i$it", ItemCategory.LAMP) }
        val outcomes = items.mapIndexed { i, it -> outcome(it.id, if (i == 0) FIXED else NOT_FIXED) }
        assertEquals(13, outcomeStats(items, outcomes).repairRatePercent) // 12.5 % -> 13
    }

    @Test fun onlyAdviceGivesNoRate() {
        val items = listOf(item("a", ItemCategory.CLOCK))
        assertNull(outcomeStats(items, listOf(outcome("a", ADVICE))).repairRatePercent)
        assertNull(outcomeStats(items, emptyList()).repairRatePercent)
    }

    @Test fun ratesPerCategoryOnlyForCategoriesWithAttempts() {
        val items = listOf(
            item("lamp1", ItemCategory.LAMP), item("lamp2", ItemCategory.LAMP),
            item("bike", ItemCategory.BICYCLE), item("clock", ItemCategory.CLOCK),
        )
        val outcomes = listOf(
            outcome("lamp1", FIXED), outcome("lamp2", NOT_FIXED),
            outcome("bike", PARTIAL), outcome("clock", ADVICE),
        )
        assertEquals(
            mapOf(ItemCategory.LAMP to 50, ItemCategory.BICYCLE to 100),
            outcomeStats(items, outcomes).repairRateByCategory,
        )
    }
}
