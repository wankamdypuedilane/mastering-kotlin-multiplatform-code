// hearthseed/core/model/src/commonMain/kotlin/hearthseed/core/model/Loan.kt
package hearthseed.core.model

/** A member and the date they joined the library. */
data class Member(val id: String, val displayName: String, val joinedOn: String)

/**
 * A lot borrowed by a member. Dates stay ISO-8601 strings ("2026-11-08"):
 * the app only displays them, and kotlinx-datetime is still Alpha.
 */
data class Loan(
    val id: String,
    val lotId: String,
    val memberId: String,
    val borrowedOn: String,
    val dueSeason: Int, // the year whose harvest is returned
    val returnedGrams: Int?,
) {
    val isOpen: Boolean get() = returnedGrams == null
}
