package seedloans

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

/** A working in-memory source: tests change loans and observers see it. */
class FakeLoanSource(vararg initial: Loan) : LoanSource {
    private val all = MutableStateFlow(initial.toList())

    override fun loans(memberId: String): Flow<List<Loan>> =
        all.map { loans -> loans.filter { it.memberId == memberId } }

    fun add(loan: Loan) = all.update { it + loan }

    fun returnLoan(id: String, grams: Int) = all.update { loans ->
        loans.map { if (it.id == id) it.copy(returnedGrams = grams) else it }
    }
}

/** A clock that only moves when the test says so. */
class FakeToday(var date: LocalDate) : Today {
    override fun date(): LocalDate = date
}

/** A hand-written fake that records what was sent. */
class RecordingSender(private val failFor: Set<String> = emptySet()) :
    ReminderSender {
    val sent = mutableListOf<Pair<String, String>>()

    override suspend fun send(memberId: String, text: String): Boolean {
        if (memberId in failFor) return false
        sent += memberId to text
        return true
    }
}

fun loan(id: String, member: String = "m-ada", season: Int = 2026) = Loan(
    id = id,
    lotId = "L-$id",
    memberId = member,
    borrowedOn = LocalDate(season, 3, 14),
    dueSeason = season,
)
