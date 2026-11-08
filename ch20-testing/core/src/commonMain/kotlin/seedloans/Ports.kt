// ch20-testing/core/src/commonMain/kotlin/seedloans/Ports.kt
package seedloans

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** The loans of a member, as the repository observes them. */
interface LoanSource {
    fun loans(memberId: String): Flow<List<Loan>>
}

/** Today's date in the library's time zone. */
fun interface Today {
    fun date(): LocalDate
}

class SystemToday(private val zone: TimeZone) : Today {
    override fun date(): LocalDate = Clock.System.todayIn(zone)
}

/** Delivers a reminder (push, e-mail...); false when delivery failed. */
interface ReminderSender {
    suspend fun send(memberId: String, text: String): Boolean
}
