// hearthseed/core/data/src/commonMain/kotlin/hearthseed/core/data/CatalogRepository.kt
package hearthseed.core.data

import hearthseed.core.data.local.VarietyLocalSource
import hearthseed.core.data.remote.toDataResult
import hearthseed.core.data.remote.toDomain
import hearthseed.core.data.remote.toDomainSkippingInvalid
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety
import hearthseed.core.network.HearthseedApi
import kotlinx.coroutines.flow.Flow

/** The seed catalog, as the screens see it. */
interface CatalogRepository {
    /** One page of varieties, filtered by the server. */
    suspend fun varieties(
        query: String,
        family: String?,
        page: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): DataResult<VarietyPage>

    /** One variety: the local copy if the list loaded it, else the network. */
    suspend fun variety(id: String): DataResult<Variety>

    /** The lots of a variety, kept current by refreshes and borrows. */
    fun observeLots(varietyId: String): Flow<List<SeedLot>?>

    /** Loads the lots from the network into the local source. */
    suspend fun refreshLots(varietyId: String): DataResult<List<SeedLot>>

    companion object { const val DEFAULT_PAGE_SIZE = 20 }
}

class DefaultCatalogRepository(
    private val api: HearthseedApi,
    private val local: VarietyLocalSource,
) : CatalogRepository {
    override suspend fun varieties(
        query: String,
        family: String?,
        page: Int,
        pageSize: Int,
    ): DataResult<VarietyPage> {
        val result = api.varieties(
            query = query.trim().ifEmpty { null }, // blank = no filter
            family = family, page = page, pageSize = pageSize,
        ).toDataResult { dto ->
            VarietyPage(dto.items.toDomainSkippingInvalid(), dto.page,
                dto.pageSize, dto.total)
        }
        if (result is DataResult.Success) {
            local.saveVarieties(result.value.items) // for detail and offline
        }
        return result
    }

    override suspend fun variety(id: String): DataResult<Variety> {
        local.variety(id)?.let { return DataResult.Success(it) }
        val result = api.variety(id).toDataResult { it.toDomain() }
        if (result is DataResult.Success) {
            local.saveVarieties(listOf(result.value))
        }
        return result
    }

    override fun observeLots(varietyId: String): Flow<List<SeedLot>?> =
        local.observeLots(varietyId)

    override suspend fun refreshLots(
        varietyId: String,
    ): DataResult<List<SeedLot>> {
        // Lots change when members borrow: always ask the network.
        val result = api.lots(varietyId).toDataResult { lots ->
            lots.map { it.toDomain() }
        }
        if (result is DataResult.Success) {
            local.saveLots(varietyId, result.value) // observers update
        }
        return result
    }
}
