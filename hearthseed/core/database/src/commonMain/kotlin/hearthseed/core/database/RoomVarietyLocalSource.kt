// hearthseed/core/database/src/commonMain/kotlin/hearthseed/core/database/RoomVarietyLocalSource.kt
package hearthseed.core.database

import hearthseed.core.data.local.VarietyLocalSource
import hearthseed.core.model.LotStatus
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** The device copy of the catalog, in Room: it survives restarts. */
class RoomVarietyLocalSource(
    database: HearthseedDatabase,
    private val clock: Clock = Clock.System,
) : VarietyLocalSource {
    private val varieties = database.varietyDao()
    private val lots = database.lotDao()

    override suspend fun variety(id: String): Variety? =
        varieties.byId(id)?.toDomain()

    override suspend fun saveVarieties(varieties: List<Variety>) =
        this.varieties.upsert(varieties.map { it.toEntity() })

    override suspend fun search(query: String, family: String?): List<Variety> =
        varieties.search(query, family).map { it.toDomain() }

    override fun observeLots(varietyId: String): Flow<List<SeedLot>?> =
        lots.observeLotsOf(varietyId).map { stored ->
            // @Relation promises no order: sort for a stable list.
            stored?.lots?.map { it.toDomain() }?.sortedBy { it.id }
        }.distinctUntilChanged() // a write elsewhere may not change these lots

    override suspend fun lot(lotId: String): SeedLot? =
        lots.byId(lotId)?.toDomain()

    override suspend fun saveLots(varietyId: String, lots: List<SeedLot>) =
        this.lots.replaceLots(LotRefreshEntity(varietyId, clock.now()),
            lots.map { it.toEntity() })

    override suspend fun updateLotStatus(lotId: String, status: LotStatus) =
        lots.updateStatus(lotId, status)
}
