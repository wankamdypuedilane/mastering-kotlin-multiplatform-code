// hearthseed/core/data/src/commonMain/kotlin/hearthseed/core/data/VarietyPage.kt
package hearthseed.core.data

import hearthseed.core.model.Variety

/** One page of the catalog: [page] starts at 1, [total] counts all matches. */
data class VarietyPage(
    val items: List<Variety>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
) {
    /** Computed from the server's numbers, not from items.size: the mapper
     *  may have dropped an invalid item. */
    val hasMore: Boolean get() = page * pageSize < total
}
