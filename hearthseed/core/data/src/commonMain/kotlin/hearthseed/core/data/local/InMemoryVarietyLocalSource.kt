// hearthseed/core/data/src/commonMain/kotlin/hearthseed/core/data/local/InMemoryVarietyLocalSource.kt
package hearthseed.core.data.local

import hearthseed.core.model.LotStatus
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** A [VarietyLocalSource] in memory: it lives as long as the process. */
class InMemoryVarietyLocalSource : VarietyLocalSource {
    private val varieties = MutableStateFlow<Map<String, Variety>>(emptyMap())
    private val lotsByVariety = MutableStateFlow<Map<String, List<SeedLot>>>(emptyMap())

    override suspend fun variety(id: String): Variety? = varieties.value[id]

    override suspend fun saveVarieties(varieties: List<Variety>) {
        this.varieties.update { old -> old + varieties.associateBy { it.id } }
    }

    override suspend fun search(query: String, family: String?): List<Variety> =
        varieties.value.values
            .filter { family == null || it.family.equals(family, ignoreCase = true) }
            .filter {
                it.commonName.contains(query, ignoreCase = true) ||
                    it.latinName.contains(query, ignoreCase = true)
            }
            .sortedBy { it.id }

    override fun observeLots(varietyId: String): Flow<List<SeedLot>?> =
        lotsByVariety.map { it[varietyId] }.distinctUntilChanged()

    override suspend fun lot(lotId: String): SeedLot? =
        lotsByVariety.value.values.flatten().find { it.id == lotId }

    override suspend fun saveLots(varietyId: String, lots: List<SeedLot>) {
        lotsByVariety.update { it + (varietyId to lots) }
    }

    override suspend fun updateLotStatus(lotId: String, status: LotStatus) {
        lotsByVariety.update { all ->
            all.mapValues { (_, lots) ->
                lots.map { if (it.id == lotId) it.copy(status = status) else it }
            }
        }
    }
}
