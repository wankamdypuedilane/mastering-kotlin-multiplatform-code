package hearthseed.data.remote

import kotlinx.serialization.Serializable

/** The JSON shape of GET /api/v1/varieties and /api/v1/varieties/{id}. */
@Serializable
data class VarietyDto(
    val id: String,
    val commonName: String,
    val latinName: String,
    val family: String,
    val daysToMaturity: Int,
    val sowingMonths: List<Int>,
    val difficulty: String,
    val description: String = "",
)

/** The JSON shape of GET /api/v1/varieties/{id}/lots. */
@Serializable
data class SeedLotDto(
    val id: String,
    val varietyId: String,
    val harvestYear: Int,
    val grams: Int,
    val germinationRate: Int,
    val donorMemberId: String? = null,
    val status: String,
)

/** The body the server sends with an error status. */
@Serializable
data class ErrorDto(val code: String, val message: String)
