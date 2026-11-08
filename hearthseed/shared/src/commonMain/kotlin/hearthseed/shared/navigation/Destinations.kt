// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/navigation/Destinations.kt
package hearthseed.shared.navigation

import kotlinx.serialization.Serializable

// Type-safe routes (Chapter 6): each destination and its arguments.
@Serializable data object Catalog
@Serializable data class VarietyDetail(val varietyId: String)
@Serializable data class Borrow(val lotId: String, val lotSummary: String)
@Serializable data object MyLoans
