package seedloans

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Larkmoor rule: harvested seed is due on 30 November of the due season. */
fun Loan.dueDate(): LocalDate = LocalDate(dueSeason, 11, 30)

/** How many days before the due date a loan counts as "due soon". */
const val DUE_SOON_DAYS = 30

sealed interface LoanStatus {
    data object Returned : LoanStatus
    data class Open(val daysLeft: Int) : LoanStatus
    data class DueSoon(val daysLeft: Int) : LoanStatus
    data class Overdue(val daysLate: Int) : LoanStatus
}

fun Loan.statusOn(today: LocalDate): LoanStatus {
    if (returnedGrams != null) return LoanStatus.Returned
    val daysLeft = today.daysUntil(dueDate())
    return when {
        daysLeft < 0 -> LoanStatus.Overdue(daysLate = -daysLeft)
        daysLeft <= DUE_SOON_DAYS -> LoanStatus.DueSoon(daysLeft)
        else -> LoanStatus.Open(daysLeft)
    }
}

/** Text for the reminders screen (Exercise 20.1 improves it). */
fun LoanStatus.describe(): String = when (this) {
    LoanStatus.Returned -> "Returned"
    is LoanStatus.Open -> "Due in $daysLeft days"
    is LoanStatus.DueSoon -> "Due in $daysLeft days"
    is LoanStatus.Overdue -> "Overdue by $daysLate days"
}
