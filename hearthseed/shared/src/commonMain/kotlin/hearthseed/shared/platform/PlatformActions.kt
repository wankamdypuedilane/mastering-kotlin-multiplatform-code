// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/platform/PlatformActions.kt
package hearthseed.shared.platform

import androidx.compose.runtime.staticCompositionLocalOf
import hearthseed.core.model.Loan
import hearthseed.core.model.Variety

/** Hands text to the platform's share sheet (Chapter 17). */
fun interface Sharer {
    fun share(text: String)
}

/** Reminds the member of the lots they are growing (Chapter 17). */
fun interface LoanReminders {
    fun remind(openLoans: List<Loan>)
}

// Provided by the platform's entry point, which owns the window or activity.
// null: the platform has no such action, and the UI hides its button.
val LocalSharer = staticCompositionLocalOf<Sharer?> { null }
val LocalLoanReminders = staticCompositionLocalOf<LoanReminders?> { null }

/** What a member sends to a friend about a variety. */
fun shareText(variety: Variety): String =
    "${variety.commonName} (${variety.latinName}): ready in " +
        "${variety.daysToMaturity} days. Borrow it from the Larkmoor " +
        "Commons Seed Library."

/** The text of a reminder, or null when nothing is growing. */
fun reminderText(openLoans: List<Loan>): String? {
    if (openLoans.isEmpty()) return null
    val lots = if (openLoans.size == 1) "1 lot" else "${openLoans.size} lots"
    val season = openLoans.minOf { it.dueSeason }
    return "You are growing $lots. Save seed for the $season return."
}
