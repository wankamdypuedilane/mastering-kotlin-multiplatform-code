// hearthseed/core/network/src/commonMain/kotlin/hearthseed/core/network/Dtos.kt
package hearthseed.core.network

import kotlinx.serialization.Serializable

// Client copies of the hearthseed-server contract (see its README.md).

@Serializable
enum class Difficulty { EASY, MEDIUM, HARD }

@Serializable
enum class LotStatus { AVAILABLE, ON_LOAN, RETIRED }

@Serializable
data class VarietyDto(
    val id: String,
    val commonName: String,
    val latinName: String,
    val family: String,
    val daysToMaturity: Int,
    val sowingMonths: List<Int>,
    val difficulty: Difficulty,
    val description: String,
)

@Serializable
data class SeedLotDto(
    val id: String,
    val varietyId: String,
    val harvestYear: Int,
    val grams: Int,
    val germinationRate: Int,
    val donorMemberId: String? = null,
    val status: LotStatus,
)

@Serializable
data class LoanDto(
    val id: String,
    val lotId: String,
    val memberId: String,
    val borrowedOn: String,
    val dueSeason: Int,
    val returnedGrams: Int? = null,
)

@Serializable
data class LoanRequest(val lotId: String, val memberId: String)

@Serializable
data class Page<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
)

@Serializable
data class ErrorDto(val code: String, val message: String)

@Serializable
data class HealthDto(val status: String, val varieties: Int)
