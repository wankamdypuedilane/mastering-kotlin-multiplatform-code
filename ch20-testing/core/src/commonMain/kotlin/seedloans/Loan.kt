package seedloans

import kotlinx.datetime.LocalDate

/** A seed lot lent to a member; harvested seed comes back in [dueSeason]. */
data class Loan(
    val id: String,
    val lotId: String,
    val memberId: String,
    val borrowedOn: LocalDate,
    val dueSeason: Int,
    val returnedGrams: Int? = null,
)
