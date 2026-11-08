// hearthseed/core/testing/src/commonMain/kotlin/hearthseed/core/testing/FakeLoanRepository.kt
package hearthseed.core.testing

import hearthseed.core.data.DataError
import hearthseed.core.data.DataResult
import hearthseed.core.data.LoanRepository
import hearthseed.core.model.Loan
import kotlin.time.Duration
import kotlinx.coroutines.delay

/** Lends each lot once; a second borrow of the same lot is a conflict. */
class FakeLoanRepository(
    private val catalog: FakeCatalogRepository? = null,
    private val memberId: String = "mem-003",
) : LoanRepository {
    var failure: DataError? = null
    var latency: Duration = Duration.ZERO
    val loans = mutableListOf<Loan>()
    /** Lots that "another member" has already taken. */
    val takenLots = mutableSetOf<String>()

    override suspend fun borrow(lotId: String): DataResult<Loan> {
        delay(latency)
        failure?.let { return DataResult.Failure(it) }
        if (lotId in takenLots) return DataResult.Failure(DataError.LotUnavailable)
        takenLots += lotId
        val loan = Loan("loan-${loans.size + 100}", lotId, memberId,
            "2026-11-08", 2026, null)
        loans += loan
        catalog?.markOnLoan(lotId)
        return DataResult.Success(loan)
    }

    override suspend fun myLoans(): DataResult<List<Loan>> {
        delay(latency)
        return failure?.let { DataResult.Failure(it) } ?: DataResult.Success(loans.toList())
    }
}
