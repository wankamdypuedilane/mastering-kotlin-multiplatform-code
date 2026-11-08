package hearthseed.data.remote

import hearthseed.data.model.Difficulty
import hearthseed.data.model.LotStatus
import hearthseed.data.model.SeedLot
import hearthseed.data.model.Variety

/** A DTO that cannot become a valid domain object. */
class InvalidDataException(message: String) : Exception(message)

private fun invalid(message: String): Nothing =
    throw InvalidDataException(message)

fun VarietyDto.toDomain(): Variety {
    if (id.isBlank()) invalid("variety without id")
    if (commonName.isBlank()) invalid("variety $id has no name")
    if (daysToMaturity <= 0) invalid("variety $id: days=$daysToMaturity")
    val months = sowingMonths.toSet()
    if (months.any { it !in 1..12 }) invalid("variety $id: $sowingMonths")
    val level = Difficulty.entries.find { it.name == difficulty }
        ?: invalid("variety $id: difficulty=$difficulty")
    return Variety(
        id = id, commonName = commonName.trim(), latinName = latinName.trim(),
        family = family.trim(), daysToMaturity = daysToMaturity,
        sowingMonths = months, difficulty = level, description = description,
    )
}

/** Maps a list, dropping the items that fail validation. */
fun List<VarietyDto>.toDomainSkippingInvalid(): List<Variety> = mapNotNull {
    try { it.toDomain() } catch (e: InvalidDataException) { null }
}

fun SeedLotDto.toDomain(): SeedLot {
    if (grams <= 0) invalid("lot $id: grams=$grams")
    if (germinationRate !in 0..100) invalid("lot $id: rate=$germinationRate")
    val lotStatus = LotStatus.entries.find { it.name == status }
        ?: invalid("lot $id: status=$status")
    return SeedLot(
        id = id,
        varietyId = varietyId,
        harvestYear = harvestYear,
        grams = grams,
        germinationRate = germinationRate,
        donorMemberId = donorMemberId?.takeIf { it.isNotBlank() },
        status = lotStatus,
    )
}
