// hearthseed/core/database/src/commonMain/kotlin/hearthseed/core/database/Mappers.kt
package hearthseed.core.database

import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety

internal fun Variety.toEntity() = VarietyEntity(id, commonName, latinName,
    family, daysToMaturity, sowingMonths, difficulty, description)

internal fun VarietyEntity.toDomain() = Variety(id, commonName, latinName,
    family, daysToMaturity, sowingMonths, difficulty, description)

internal fun SeedLot.toEntity() = SeedLotEntity(id, varietyId, harvestYear,
    grams, germinationRate, donorMemberId, status)

internal fun SeedLotEntity.toDomain() = SeedLot(id, varietyId, harvestYear,
    grams, germinationRate, donorMemberId, status)
