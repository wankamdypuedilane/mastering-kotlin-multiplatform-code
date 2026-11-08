// hearthseed/core/database/src/commonMain/kotlin/hearthseed/core/database/Daos.kt
package hearthseed.core.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import hearthseed.core.model.LotStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface VarietyDao {
    @Upsert // insert new ids, update known ones
    suspend fun upsert(varieties: List<VarietyEntity>)

    @Query("SELECT * FROM variety WHERE id = :id")
    suspend fun byId(id: String): VarietyEntity?

    @Query(
        """
        SELECT * FROM variety
        WHERE (:family IS NULL OR family = :family COLLATE NOCASE)
          AND (instr(lower(common_name), lower(:query)) > 0
               OR instr(lower(latin_name), lower(:query)) > 0)
        ORDER BY id
        """,
    )
    suspend fun search(query: String, family: String?): List<VarietyEntity>
}

@Dao
interface LotDao {
    /** Emits again after every write to lot_refresh or seed_lot. */
    @Transaction // the refresh row and its lots are read together
    @Query("SELECT * FROM lot_refresh WHERE variety_id = :varietyId")
    fun observeLotsOf(varietyId: String): Flow<LotsOfVariety?>

    @Query("SELECT * FROM seed_lot WHERE id = :lotId")
    suspend fun byId(lotId: String): SeedLotEntity?

    @Query("UPDATE seed_lot SET status = :status WHERE id = :lotId")
    suspend fun updateStatus(lotId: String, status: LotStatus)

    @Upsert suspend fun upsertRefresh(refresh: LotRefreshEntity)

    @Query("DELETE FROM seed_lot WHERE variety_id = :varietyId")
    suspend fun deleteLotsOf(varietyId: String)

    @Insert suspend fun insertLots(lots: List<SeedLotEntity>)

    /** All or nothing: observers never see a half-replaced list. */
    @Transaction
    suspend fun replaceLots(
        refresh: LotRefreshEntity,
        lots: List<SeedLotEntity>,
    ) {
        upsertRefresh(refresh) // the parent row first (foreign key)
        deleteLotsOf(refresh.varietyId)
        insertLots(lots)
    }
}
