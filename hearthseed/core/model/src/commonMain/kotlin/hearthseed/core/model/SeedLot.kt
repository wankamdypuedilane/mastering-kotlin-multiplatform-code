// hearthseed/core/model/src/commonMain/kotlin/hearthseed/core/model/SeedLot.kt
package hearthseed.core.model

/** Where a seed lot is in its life at the library. */
enum class LotStatus { AVAILABLE, ON_LOAN, RETIRED }

/** One physical batch of seed of a variety, harvested in a given year. */
data class SeedLot(
    val id: String,
    val varietyId: String,
    val harvestYear: Int,
    val grams: Int,
    val germinationRate: Int, // percent, 0..100
    val donorMemberId: String?,
    val status: LotStatus,
) {
    val isBorrowable: Boolean get() = status == LotStatus.AVAILABLE
}
