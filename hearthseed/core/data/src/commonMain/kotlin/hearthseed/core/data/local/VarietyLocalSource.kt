// hearthseed/core/data/src/commonMain/kotlin/hearthseed/core/data/local/VarietyLocalSource.kt
package hearthseed.core.data.local

import hearthseed.core.model.LotStatus
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety
import kotlinx.coroutines.flow.Flow

/**
 * What the repositories keep on the device. Chapter 12 uses the in-memory
 * implementation; Chapter 14 implements the same interface with Room, and
 * nothing above the repositories changes.
 */
interface VarietyLocalSource {
    suspend fun variety(id: String): Variety?
    suspend fun saveVarieties(varieties: List<Variety>)

    /** Varieties seen so far that match, for an offline search. */
    suspend fun search(query: String, family: String?): List<Variety>

    /** A variety's lots now and after every change; null = never loaded. */
    fun observeLots(varietyId: String): Flow<List<SeedLot>?>
    suspend fun lot(lotId: String): SeedLot?
    suspend fun saveLots(varietyId: String, lots: List<SeedLot>)
    suspend fun updateLotStatus(lotId: String, status: LotStatus)
}
