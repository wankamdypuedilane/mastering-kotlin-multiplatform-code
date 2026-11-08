package hearthseed.data.model

/** Where a seed lot is in its life at the library. */
enum class LotStatus { AVAILABLE, ON_LOAN, RETIRED }

/** One physical batch of seed of a variety, harvested in a given year. */
data class SeedLot(
    val id: String,
    val varietyId: String,
    val harvestYear: Int,
    val grams: Int,
    val germinationRate: Int,
    val donorMemberId: String?,
    val status: LotStatus,
)
