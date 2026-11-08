// hearthseed/core/testing/src/commonMain/kotlin/hearthseed/core/testing/FakeCatalogRepository.kt
package hearthseed.core.testing

import hearthseed.core.data.CatalogRepository
import hearthseed.core.data.DataError
import hearthseed.core.data.DataResult
import hearthseed.core.data.VarietyPage
import hearthseed.core.model.LotStatus
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** A working catalog in memory, filtered and paged like the server. */
class FakeCatalogRepository(
    var varieties: List<Variety> = SampleData.varieties,
    private val serverLots: Map<String, List<SeedLot>> = SampleData.lots,
) : CatalogRepository {
    /** Returned by every call while set (e.g. DataError.Offline). */
    var failure: DataError? = null
    /** Virtual time under runTest: lets a test see the loading states. */
    var latency: Duration = Duration.ZERO

    data class PageRequest(val query: String, val family: String?, val page: Int)
    val pageRequests = mutableListOf<PageRequest>()

    private val localLots = MutableStateFlow<Map<String, List<SeedLot>>>(emptyMap())

    private suspend fun <T> answer(value: () -> T): DataResult<T> {
        delay(latency)
        return failure?.let { DataResult.Failure(it) } ?: DataResult.Success(value())
    }

    override suspend fun varieties(
        query: String, family: String?, page: Int, pageSize: Int,
    ): DataResult<VarietyPage> {
        pageRequests += PageRequest(query, family, page)
        return answer {
            val matches = varieties.filter {
                (family == null || it.family == family) &&
                    it.commonName.contains(query.trim(), ignoreCase = true)
            }
            val items = matches.drop((page - 1) * pageSize).take(pageSize)
            VarietyPage(items, page, pageSize, matches.size)
        }
    }

    override suspend fun variety(id: String): DataResult<Variety> {
        delay(latency)
        failure?.let { return DataResult.Failure(it) }
        val found = varieties.find { it.id == id }
        return found?.let { DataResult.Success(it) } ?: DataResult.Failure(DataError.NotFound)
    }

    override fun observeLots(varietyId: String): Flow<List<SeedLot>?> =
        localLots.map { it[varietyId] }

    override suspend fun refreshLots(varietyId: String): DataResult<List<SeedLot>> =
        answer { serverLots[varietyId].orEmpty() }
            .also { result ->
                if (result is DataResult.Success) {
                    localLots.update { it + (varietyId to result.value) }
                }
            }

    /** What a successful borrow does to the local copy. */
    fun markOnLoan(lotId: String) = localLots.update { all ->
        all.mapValues { (_, lots) ->
            lots.map { if (it.id == lotId) it.copy(status = LotStatus.ON_LOAN) else it }
        }
    }
}
