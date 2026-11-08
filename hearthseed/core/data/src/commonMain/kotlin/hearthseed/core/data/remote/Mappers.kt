// hearthseed/core/data/src/commonMain/kotlin/hearthseed/core/data/remote/Mappers.kt
package hearthseed.core.data.remote

import hearthseed.core.data.DataError
import hearthseed.core.data.DataResult
import hearthseed.core.model.Difficulty
import hearthseed.core.model.Loan
import hearthseed.core.model.LotStatus
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety
import hearthseed.core.network.ApiError
import hearthseed.core.network.ApiResult
import hearthseed.core.network.LoanDto
import hearthseed.core.network.SeedLotDto
import hearthseed.core.network.VarietyDto

/** A DTO that cannot become a valid domain object (Chapter 10). */
internal class InvalidDataException(message: String) : Exception(message)

private fun invalid(message: String): Nothing =
    throw InvalidDataException(message)

internal fun VarietyDto.toDomain(): Variety {
    if (id.isBlank() || commonName.isBlank()) invalid("variety without id/name")
    if (daysToMaturity <= 0) invalid("variety $id: days=$daysToMaturity")
    if (sowingMonths.any { it !in 1..12 }) invalid("variety $id: $sowingMonths")
    val level = Difficulty.entries.find { it.name == difficulty }
        ?: invalid("variety $id: difficulty=$difficulty")
    return Variety(
        id = id, commonName = commonName.trim(), latinName = latinName.trim(),
        family = family.trim(), daysToMaturity = daysToMaturity,
        sowingMonths = sowingMonths.toSet(), difficulty = level,
        description = description,
    )
}

/** In a list, one broken item is dropped instead of failing the page. */
internal fun List<VarietyDto>.toDomainSkippingInvalid(): List<Variety> =
    mapNotNull { dto ->
        try { dto.toDomain() } catch (_: InvalidDataException) { null }
    }

internal fun SeedLotDto.toDomain(): SeedLot {
    if (grams <= 0) invalid("lot $id: grams=$grams")
    if (germinationRate !in 0..100) invalid("lot $id: rate=$germinationRate")
    val lotStatus = LotStatus.entries.find { it.name == status }
        ?: invalid("lot $id: status=$status")
    return SeedLot(
        id = id, varietyId = varietyId, harvestYear = harvestYear,
        grams = grams, germinationRate = germinationRate,
        donorMemberId = donorMemberId?.takeIf { it.isNotBlank() },
        status = lotStatus,
    )
}

/** The same rule for lots: an unknown status drops that lot only. */
internal fun List<SeedLotDto>.toLotsSkippingInvalid(): List<SeedLot> =
    mapNotNull { dto ->
        try { dto.toDomain() } catch (_: InvalidDataException) { null }
    }

internal fun LoanDto.toDomain() = Loan(
    id = id, lotId = lotId, memberId = memberId, borrowedOn = borrowedOn,
    dueSeason = dueSeason, returnedGrams = returnedGrams,
)

/** Chapter 9's network errors, in the words of the screens. */
internal fun ApiError.toDataError(): DataError = when (this) {
    is ApiError.Network, ApiError.Timeout -> DataError.Offline
    is ApiError.NotFound -> DataError.NotFound
    is ApiError.Conflict -> DataError.LotUnavailable
    is ApiError.Unauthorized -> DataError.Unauthorized
    is ApiError.Server -> DataError.Server(status)
    is ApiError.BadRequest -> DataError.Unexpected("Bad request: $message")
    is ApiError.Unexpected -> DataError.Unexpected(message)
}

/** Maps a network result; a DTO that fails validation becomes Unexpected. */
internal inline fun <T, R> ApiResult<T>.toDataResult(
    transform: (T) -> R,
): DataResult<R> = when (this) {
    is ApiResult.Failure -> DataResult.Failure(error.toDataError())
    is ApiResult.Success -> try {
        DataResult.Success(transform(value))
    } catch (e: InvalidDataException) {
        DataResult.Failure(DataError.Unexpected(e.message ?: "invalid data"))
    }
}
