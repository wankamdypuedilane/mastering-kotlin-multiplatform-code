package seedloans

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class DueRulesTest {
    private val loan = loan("1", season = 2026)    // due 30 Nov 2026

    // One row per boundary: the day of the date, the expected status.
    private val cases = listOf(
        LocalDate(2026, 10, 30) to LoanStatus.Open(daysLeft = 31),
        LocalDate(2026, 10, 31) to LoanStatus.DueSoon(daysLeft = 30),
        LocalDate(2026, 11, 30) to LoanStatus.DueSoon(daysLeft = 0),
        LocalDate(2026, 12, 1) to LoanStatus.Overdue(daysLate = 1),
        LocalDate(2027, 1, 1) to LoanStatus.Overdue(daysLate = 32),
    )

    @Test
    fun statusFollowsTheDueDate() {
        for ((today, expected) in cases) {
            assertEquals(expected, loan.statusOn(today), "on $today")
        }
    }

    @Test
    fun aReturnedLoanIsNeverOverdue() {
        val returned = loan.copy(returnedGrams = 40)
        assertEquals(LoanStatus.Returned,
            returned.statusOn(LocalDate(2030, 1, 1)))
    }

    @Test
    fun describeNamesTheStatus() {
        assertEquals("Due in 31 days", LoanStatus.Open(31).describe())
        assertEquals("Due in 3 days", LoanStatus.DueSoon(3).describe())
        assertEquals("Overdue by 4 days", LoanStatus.Overdue(4).describe())
        assertEquals("Returned", LoanStatus.Returned.describe())
    }
}
