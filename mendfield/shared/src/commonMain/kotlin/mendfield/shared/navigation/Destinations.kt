// mendfield/shared/src/commonMain/kotlin/mendfield/shared/navigation/Destinations.kt
package mendfield.shared.navigation

import kotlinx.serialization.Serializable

// Type-safe routes: each destination with its arguments, checked by the
// compiler. Only ids travel; each screen reads the rest from the cache.
@Serializable data object Events
@Serializable data class Board(val eventId: String)
@Serializable data class Intake(val eventId: String)
@Serializable data class LogOutcome(val itemId: String)
@Serializable data class Stats(val eventId: String)
