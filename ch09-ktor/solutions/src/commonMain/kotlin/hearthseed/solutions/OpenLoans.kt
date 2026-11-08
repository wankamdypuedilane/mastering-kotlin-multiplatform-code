// ch09-ktor/solutions/src/commonMain/kotlin/hearthseed/solutions/OpenLoans.kt
package hearthseed.solutions

import hearthseed.network.ApiResult
import hearthseed.network.HearthseedApi
import hearthseed.network.LoanDto

/** Transforms a success and passes a failure through unchanged. */
inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> =
    when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(value))
        is ApiResult.Failure -> this
    }

/** Exercise 9.2: the loans whose seed has not come back yet. */
suspend fun HearthseedApi.openLoans(memberId: String): ApiResult<List<LoanDto>> =
    loansOf(memberId).map { loans -> loans.filter { it.returnedGrams == null } }
