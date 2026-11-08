package seedloans

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

data class Reminder(val loan: Loan, val status: LoanStatus)

/** Loans that need attention, re-checked once a day while observed. */
class LoanReminder(
    private val source: LoanSource,
    private val today: Today,
    private val checkEvery: Duration = 1.days,
) {
    fun reminders(memberId: String): Flow<List<Reminder>> =
        combine(source.loans(memberId), ticks()) { loans, _ ->
            val date = today.date()
            loans.map { Reminder(it, it.statusOn(date)) }
                .filter { it.status is LoanStatus.DueSoon ||
                    it.status is LoanStatus.Overdue }
                .sortedBy { it.loan.dueDate() }
        }.distinctUntilChanged()

    private fun ticks(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(checkEvery)          // 1 day by default
        }
    }
}
