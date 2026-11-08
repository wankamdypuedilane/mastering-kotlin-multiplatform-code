// ch20-testing/core/src/commonTest/kotlin/seedloans/LoanReminderTest.kt
@file:OptIn(ExperimentalCoroutinesApi::class)

package seedloans

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days

class LoanReminderTest {
    private val today = FakeToday(LocalDate(2026, 11, 27))
    private val source = FakeLoanSource(loan("1"), loan("2", season = 2027))
    private val reminder = LoanReminder(source, today)

    @Test
    fun aLoanBecomesOverdueAfterItsDueDate() = runTest {
        reminder.reminders("m-ada").test {
            assertEquals(listOf(LoanStatus.DueSoon(3)),
                awaitItem().map { it.status })

            today.date = LocalDate(2026, 12, 1)
            // The next daily check runs after 1 day of virtual time.
            assertEquals(listOf(LoanStatus.Overdue(1)),
                awaitItem().map { it.status })
            assertEquals(1.days.inWholeMilliseconds, testScheduler.currentTime)
        }
    }

    @Test
    fun unchangedResultsAreNotEmittedAgain() = runTest {
        reminder.reminders("m-ada").test {
            awaitItem()
            testScheduler.advanceTimeBy(1.days)   // same date, same list
            expectNoEvents()
        }
    }

    @Test
    fun aNewLoanShowsUpWithoutWaitingForTheNextCheck() = runTest {
        reminder.reminders("m-ada").test {
            assertEquals(1, awaitItem().size)
            source.add(loan("3"))
            assertEquals(listOf("1", "3"), awaitItem().map { it.loan.id })
        }
    }
}
