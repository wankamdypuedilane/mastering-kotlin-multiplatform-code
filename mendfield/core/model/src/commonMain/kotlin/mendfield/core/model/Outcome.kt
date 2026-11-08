// mendfield/core/model/src/commonMain/kotlin/mendfield/core/model/Outcome.kt
package mendfield.core.model

import kotlin.time.Instant

enum class OutcomeKind {
    FIXED,     // works again
    PARTIAL,   // better, but needs a part or a second visit
    NOT_FIXED, // tried, could not repair
    ADVICE,    // no repair attempted: the visitor got advice instead
}

/** What happened to an item. A later outcome for the same item wins. */
data class Outcome(
    val id: String,
    val itemId: String,
    val kind: OutcomeKind,
    val note: String,
    val loggedAt: Instant,
)
