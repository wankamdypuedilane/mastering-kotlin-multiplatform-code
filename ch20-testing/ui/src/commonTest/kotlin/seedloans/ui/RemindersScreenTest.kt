// ch20-testing/ui/src/commonTest/kotlin/seedloans/ui/RemindersScreenTest.kt
package seedloans.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import seedloans.Loan
import seedloans.LoanStatus
import seedloans.Reminder
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RemindersScreenTest {
    private fun reminder(id: String, status: LoanStatus) = Reminder(
        Loan(id, "L-$id", "m-ada", LocalDate(2026, 3, 14), 2026), status)

    private val two = listOf(
        reminder("1", LoanStatus.Overdue(2)),
        reminder("2", LoanStatus.DueSoon(5)),
    )

    @Test
    fun eachReminderShowsItsStatus() = runComposeUiTest {
        setContent { RemindersScreen(two, onSend = { 0 }) }

        onNodeWithText("Overdue by 2 days").assertExists()
        onNodeWithTag("reminder-2").assertExists()
    }

    @Test
    fun sendingShowsHowManyWereDelivered() = runComposeUiTest {
        setContent {
            RemindersScreen(two, onSend = { delay(800); 1 })  // slow network
        }

        onNodeWithText("Send reminders").performClick()
        waitUntil(timeoutMillis = 2_000) {
            onAllNodes(hasTestTag("delivered"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("delivered").assertTextEquals("1 sent")
    }

    @Test
    fun withoutRemindersTheButtonIsDisabled() = runComposeUiTest {
        setContent { RemindersScreen(emptyList(), onSend = { 0 }) }

        onNodeWithText("Nothing to return yet").assertExists()
        onNodeWithText("Send reminders").assertIsNotEnabled()
    }
}
