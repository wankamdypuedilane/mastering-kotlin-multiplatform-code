// hearthseed/solutions/src/commonMain/kotlin/hearthseed/solutions/CatalogQuery.kt
package hearthseed.solutions

import hearthseed.core.model.BotanicalFamilies

/** Exercise 11.3: the query string of GET /api/v1/varieties, checked first. */
data class CatalogQuery(
    val query: String = "",
    val family: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
) {
    /** The family as the catalog spells it, or null if unknown. */
    private val canonicalFamily: String? = family?.let { f ->
        BotanicalFamilies.all.find { it.equals(f.trim(), ignoreCase = true) }
    }

    /** Every broken rule of the contract; empty when the query can be sent. */
    fun problems(): List<String> = buildList {
        if (page < 1) add("page must be at least 1")
        if (pageSize !in 1..50) add("pageSize must be in 1..50")
        if (family != null && canonicalFamily == null) {
            add("unknown family: $family")
        }
    }

    /** Parameters in the contract's order; blank and null values left out. */
    fun toParameters(): Map<String, String> {
        require(problems().isEmpty()) { problems().joinToString() }
        return buildMap {
            query.trim().takeIf { it.isNotEmpty() }?.let { put("query", it) }
            canonicalFamily?.let { put("family", it) }
            put("page", page.toString())
            put("pageSize", pageSize.toString())
        }
    }
}
