// ch09-ktor/solutions/src/commonMain/kotlin/hearthseed/solutions/AllPages.kt
package hearthseed.solutions

import hearthseed.network.ApiResult
import hearthseed.network.HearthseedApi
import hearthseed.network.VarietyDto

/** Exercise 9.3: follows the pages until every variety has been read. */
suspend fun HearthseedApi.allVarieties(
    family: String? = null,
    pageSize: Int = 20,
): ApiResult<List<VarietyDto>> {
    val all = mutableListOf<VarietyDto>()
    var page = 1
    while (true) {
        when (val result = varieties(family = family, page = page,
                                     pageSize = pageSize)) {
            is ApiResult.Failure -> return result // stop at the first error
            is ApiResult.Success -> {
                all += result.value.items
                val done = result.value.items.isEmpty() ||
                    all.size >= result.value.total
                if (done) return ApiResult.Success(all)
            }
        }
        page++
    }
}
