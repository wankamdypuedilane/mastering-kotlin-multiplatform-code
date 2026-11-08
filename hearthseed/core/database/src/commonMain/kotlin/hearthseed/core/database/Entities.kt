// hearthseed/core/database/src/commonMain/kotlin/hearthseed/core/database/Entities.kt
package hearthseed.core.database

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import androidx.room3.Relation
import hearthseed.core.model.Difficulty
import hearthseed.core.model.LotStatus
import kotlin.time.Instant

/** One row per variety the app has seen: a copy of the server's data. */
// v2 adds the index on family (the filter of the catalog)
@Entity(tableName = "variety", indices = [Index("family")])
data class VarietyEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "common_name") val commonName: String,
    @ColumnInfo(name = "latin_name") val latinName: String,
    val family: String,
    @ColumnInfo(name = "days_to_maturity") val daysToMaturity: Int,
    @ColumnInfo(name = "sowing_mask") val sowingMonths: Set<Int>, // v3: bits
    val difficulty: Difficulty, // enums are stored by name, as TEXT
    val description: String,
)

/** When the lots of a variety were last loaded: no row = never loaded. */
@Entity(tableName = "lot_refresh")
data class LotRefreshEntity(
    @PrimaryKey @ColumnInfo(name = "variety_id") val varietyId: String,
    @ColumnInfo(name = "refreshed_at") val refreshedAt: Instant,
)

@Entity(
    tableName = "seed_lot",
    foreignKeys = [ForeignKey(
        entity = LotRefreshEntity::class,
        parentColumns = ["variety_id"],
        childColumns = ["variety_id"],
        onDelete = ForeignKey.CASCADE, // forgetting a refresh drops its lots
    )],
    indices = [Index("variety_id")],
)
data class SeedLotEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "variety_id") val varietyId: String,
    @ColumnInfo(name = "harvest_year") val harvestYear: Int,
    val grams: Int,
    @ColumnInfo(name = "germination_rate") val germinationRate: Int,
    @ColumnInfo(name = "donor_member_id") val donorMemberId: String?,
    val status: LotStatus,
)

/** A refresh row with its lots: Room runs a second query to fill [lots]. */
data class LotsOfVariety(
    @Embedded val refresh: LotRefreshEntity,
    @Relation(parentColumns = ["variety_id"], entityColumns = ["variety_id"])
    val lots: List<SeedLotEntity>,
)
