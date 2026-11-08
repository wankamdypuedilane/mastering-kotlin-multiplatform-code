// ch09-ktor/solutions/src/commonMain/kotlin/hearthseed/solutions/BorrowAny.kt
package hearthseed.solutions

import hearthseed.network.ApiError
import hearthseed.network.ApiResult
import hearthseed.network.HearthseedApi
import hearthseed.network.LoanDto
import hearthseed.network.LotStatus

/**
 * Exercise 9.4: borrows the best available lot of a variety. A 409 means that
 * someone borrowed the lot after we listed it, so the next lot is tried.
 */
suspend fun HearthseedApi.borrowAnyLot(
    varietyId: String,
    memberId: String,
): ApiResult<LoanDto> {
    val lots = when (val result = lots(varietyId)) {
        is ApiResult.Failure -> return result
        is ApiResult.Success -> result.value
    }
    val candidates = lots
        .filter { it.status == LotStatus.AVAILABLE }
        .sortedByDescending { it.germinationRate }
    for (lot in candidates) {
        val loan = borrow(lot.id, memberId)
        val lostTheRace = loan is ApiResult.Failure && loan.error is ApiError.Conflict
        if (!lostTheRace) return loan // success, or an error worth reporting
    }
    return ApiResult.Failure(ApiError.Conflict("No available lot of $varietyId"))
}
