// ch20-testing/core/src/commonMain/kotlin/seedloans/solutions/Label.kt
package seedloans.solutions

import seedloans.LoanStatus

/** Exercise 20.1: describe() with "1 day" and "Due today". */
fun LoanStatus.label(): String = when (this) {
    LoanStatus.Returned -> "Returned"
    is LoanStatus.Open -> "Due in ${days(daysLeft)}"
    is LoanStatus.DueSoon ->
        if (daysLeft == 0) "Due today" else "Due in ${days(daysLeft)}"
    is LoanStatus.Overdue -> "Overdue by ${days(daysLate)}"
}

private fun days(n: Int) = if (n == 1) "1 day" else "$n days"
