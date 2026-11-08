// hearthseed/solutions/src/commonMain/kotlin/hearthseed/solutions/ClearFilters.kt
package hearthseed.solutions

import hearthseed.feature.catalog.list.CatalogViewModel

/**
 * Exercise 12.2: back to the whole catalog with one request. The query
 * change schedules a delayed load; the family change cancels it and loads
 * at once, with both filters already cleared.
 */
fun CatalogViewModel.clearFilters() {
    onQueryChange("")
    onFamilySelected(null)
}
