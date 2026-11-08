// ch20-testing/core/src/commonMain/kotlin/seedloans/ReminderDispatcher.kt
package seedloans

import co.touchlab.kermit.Logger

/** Sends one message per overdue loan and logs what went wrong. */
class ReminderDispatcher(
    private val sender: ReminderSender,
    private val log: Logger = Logger.withTag("Reminders"),
) {
    /** Returns how many reminders were delivered. */
    suspend fun sendOverdue(reminders: List<Reminder>): Int {
        val overdue = reminders.filter { it.status is LoanStatus.Overdue }
        var delivered = 0
        for (reminder in overdue) {
            val loan = reminder.loan
            val text = "Seed lot ${loan.lotId}: ${reminder.status.describe()}"
            if (sender.send(loan.memberId, text)) {
                delivered++
            } else {
                log.w { "Reminder for loan ${loan.id} was not delivered" }
            }
        }
        log.i { "Delivered $delivered of ${overdue.size} reminders" }
        return delivered
    }
}
