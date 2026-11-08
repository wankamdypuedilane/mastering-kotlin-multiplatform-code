// hearthseed/core/data/src/commonMain/kotlin/hearthseed/core/data/LoanRepository.kt
package hearthseed.core.data

import hearthseed.core.data.local.VarietyLocalSource
import hearthseed.core.data.remote.toDataResult
import hearthseed.core.data.remote.toDomain
import hearthseed.core.model.Loan
import hearthseed.core.model.LotStatus
import hearthseed.core.network.HearthseedApi

/** Borrowing lots and listing the loans of the current member. */
interface LoanRepository {
    suspend fun borrow(lotId: String): DataResult<Loan>
    suspend fun myLoans(): DataResult<List<Loan>>
}

class DefaultLoanRepository(
    private val api: HearthseedApi,
    private val local: VarietyLocalSource,
    private val catalog: CatalogRepository,
    private val session: MemberSession,
) : LoanRepository {
    override suspend fun borrow(lotId: String): DataResult<Loan> {
        val result = api.borrow(lotId, session.memberId)
            .toDataResult { it.toDomain() }
        when {
            // The server put the lot on loan: say so locally, no extra call.
            result is DataResult.Success ->
                local.updateLotStatus(lotId, LotStatus.ON_LOAN)
            // Someone was faster: reload the lots so screens show the truth.
            result is DataResult.Failure &&
                result.error == DataError.LotUnavailable ->
                local.lot(lotId)?.let { catalog.refreshLots(it.varietyId) }
        }
        return result
    }

    override suspend fun myLoans(): DataResult<List<Loan>> =
        api.loansOf(session.memberId).toDataResult { loans ->
            loans.map { it.toDomain() }.sortedByDescending { it.borrowedOn }
        }
}
