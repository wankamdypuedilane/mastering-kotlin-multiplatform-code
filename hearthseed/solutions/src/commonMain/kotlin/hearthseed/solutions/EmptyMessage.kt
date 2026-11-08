// hearthseed/solutions/src/commonMain/kotlin/hearthseed/solutions/EmptyMessage.kt
package hearthseed.solutions

import hearthseed.feature.catalog.list.CatalogUiState

/** Exercise 12.1: an empty list explained with the filters that caused it. */
fun CatalogUiState.emptyMessage(): String {
    val text = query.trim()
    return when {
        text.isNotEmpty() && family != null ->
            "No varieties match \"$text\" in $family."
        text.isNotEmpty() -> "No varieties match \"$text\"."
        family != null -> "No varieties in $family yet."
        else -> "The catalog is empty."
    }
}
