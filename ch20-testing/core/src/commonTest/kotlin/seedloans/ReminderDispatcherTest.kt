// ch20-testing/core/src/commonTest/kotlin/seedloans/ReminderDispatcherTest.kt
@file:OptIn(ExperimentalKermitApi::class)

package seedloans

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.TestLogWriter
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReminderDispatcherTest {
    private val overdue = Reminder(loan("1"), LoanStatus.Overdue(2))
    private val dueSoon = Reminder(loan("2"), LoanStatus.DueSoon(5))
    private val other =
        Reminder(loan("3", member = "m-bo"), LoanStatus.Overdue(9))

    @Test
    fun onlyOverdueLoansAreSent_withAMock() = runTest {
        val sender = mock<ReminderSender>()
        everySuspend { sender.send(any(), any()) } returns true

        val delivered = ReminderDispatcher(sender).sendOverdue(
            listOf(overdue, dueSoon))

        assertEquals(1, delivered)
        verifySuspend(exactly(1)) {
            sender.send("m-ada", "Seed lot L-1: Overdue by 2 days")
        }
    }

    @Test
    fun onlyOverdueLoansAreSent_withAFake() = runTest {
        val sender = RecordingSender()

        ReminderDispatcher(sender).sendOverdue(listOf(overdue, dueSoon))

        assertEquals(listOf("m-ada" to "Seed lot L-1: Overdue by 2 days"),
            sender.sent)
    }

    @Test
    fun aFailedDeliveryIsLoggedAsAWarning() = runTest {
        val logs = TestLogWriter(loggable = Severity.Verbose)
        val log = Logger(StaticConfig(logWriterList = listOf(logs)), "test")
        val sender = RecordingSender(failFor = setOf("m-bo"))

        val delivered = ReminderDispatcher(sender, log)
            .sendOverdue(listOf(overdue, other))

        assertEquals(1, delivered)
        val warning = logs.logs.single { it.severity == Severity.Warn }
        assertTrue("loan 3" in warning.message)
    }
}
