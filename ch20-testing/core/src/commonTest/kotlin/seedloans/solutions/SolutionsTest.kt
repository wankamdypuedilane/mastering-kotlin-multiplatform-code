@file:OptIn(ExperimentalCoroutinesApi::class)

package seedloans.solutions

import app.cash.turbine.test
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import seedloans.FakeLoanSource
import seedloans.FakeToday
import seedloans.LoanReminder
import seedloans.LoanStatus
import seedloans.ReminderSender
import seedloans.loan
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SolutionsTest {
    // Exercise 20.1: the table exposes "1 days"; label() fixes it.
    private val labels = listOf(
        LoanStatus.Open(31) to "Due in 31 days",
        LoanStatus.DueSoon(1) to "Due in 1 day",
        LoanStatus.DueSoon(0) to "Due today",
        LoanStatus.Overdue(1) to "Overdue by 1 day",
        LoanStatus.Overdue(12) to "Overdue by 12 days",
        LoanStatus.Returned to "Returned",
    )

    @Test
    fun labelsUseSingularAndToday() {
        for ((status, expected) in labels) {
            assertEquals(expected, status.label(), "for $status")
        }
    }

    // Exercise 20.2: a returned loan leaves the reminders at once.
    @Test
    fun aReturnedLoanLeavesTheReminders() = runTest {
        val source = FakeLoanSource(loan("1"), loan("2"))
        val today = FakeToday(LocalDate(2026, 12, 3))
        LoanReminder(source, today).reminders("m-ada").test {
            assertEquals(listOf("1", "2"), awaitItem().map { it.loan.id })
            source.returnLoan("1", grams = 55)
            assertEquals(listOf("2"), awaitItem().map { it.loan.id })
            source.returnLoan("2", grams = 30)
            assertEquals(emptyList(), awaitItem())
        }
    }

    // Exercise 20.3: two failures, then a success, in 30 s + 60 s.
    @Test
    fun retriesUntilTheDeliveryWorks() = runTest {
        val delegate = mock<ReminderSender>()
        everySuspend { delegate.send(any(), any()) } sequentiallyReturns
            listOf(false, false, true)

        assertTrue(RetryingSender(delegate).send("m-ada", "hello"))
        verifySuspend(exactly(3)) { delegate.send("m-ada", "hello") }
        assertEquals(90_000, testScheduler.currentTime)
    }

    @Test
    fun givesUpAfterTheLastAttemptWithoutWaiting() = runTest {
        val delegate = mock<ReminderSender>()
        everySuspend { delegate.send(any(), any()) } sequentiallyReturns
            listOf(false, false)

        assertFalse(RetryingSender(delegate, attempts = 2).send("m-ada", "x"))
        verifySuspend(exactly(2)) { delegate.send(any(), any()) }
        assertEquals(30_000, testScheduler.currentTime)
    }
}
