// mendfield/core/domain/src/commonMain/kotlin/mendfield/core/domain/OutcomeStats.kt
package mendfield.core.domain

import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.Outcome
import mendfield.core.model.OutcomeKind

data class OutcomeStats(
    val registered: Int,
    val withOutcome: Int,
    /** Every kind, zeros included, in declaration order. */
    val counts: Map<OutcomeKind, Int>,
    /** FIXED + PARTIAL out of attempted repairs (ADVICE excluded), or null. */
    val repairRatePercent: Int?,
    val repairRateByCategory: Map<ItemCategory, Int>,
) {
    val pending: Int get() = registered - withOutcome
}

/**
 * Statistics of one event. Only the latest outcome of each item counts
 * (by loggedAt, then id); outcomes of unknown items are ignored.
 */
fun outcomeStats(items: List<Item>, outcomes: List<Outcome>): OutcomeStats {
    val itemsById = items.associateBy { it.id }
    val latest = outcomes.filter { it.itemId in itemsById }
        .groupBy { it.itemId }
        .mapValues { (_, all) -> all.maxWith(latestFirst) }
        .values
    val byCategory = latest.groupBy { itemsById.getValue(it.itemId).category }
    return OutcomeStats(
        registered = items.size,
        withOutcome = latest.size,
        counts = OutcomeKind.entries.associateWith { kind ->
            latest.count { it.kind == kind }
        },
        repairRatePercent = repairRate(latest),
        repairRateByCategory = ItemCategory.entries
            .mapNotNull { category ->
                byCategory[category]?.let(::repairRate)?.let { category to it }
            }
            .toMap(),
    )
}

private val latestFirst = compareBy<Outcome>({ it.loggedAt }, { it.id })

/** A percentage rounded half up, in integers: the same on every target. */
private fun repairRate(outcomes: Collection<Outcome>): Int? {
    val attempted = outcomes.count { it.kind != OutcomeKind.ADVICE }
    if (attempted == 0) return null
    val repaired = outcomes.count {
        it.kind == OutcomeKind.FIXED || it.kind == OutcomeKind.PARTIAL
    }
    return (200 * repaired + attempted) / (2 * attempted)
}
