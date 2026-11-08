package seedcount

class Planner(stock: List<SeedStock>) {
    private val byKey: Map<String, SeedStock> =
        stock.associateBy { it.variety.key() }

    fun plan(rows: List<PlantingRow>): PlanResult {
        val errors = rows.mapNotNull { validate(it) }
        if (errors.isNotEmpty()) return PlanResult.Rejected(errors)

        val lines = rows
            .groupBy { it.variety.key() }
            .map { (key, sameVariety) ->
                lineFor(byKey.getValue(key), sameVariety.sumOf { it.plants })
            }
            .sortedByDescending { it.shortfall }
        return PlanResult.Ready(lines)
    }

    private fun validate(row: PlantingRow): PlanError? = when {
        row.plants <= 0 -> PlanError.InvalidPlantCount(row.variety, row.plants)
        row.variety.key() !in byKey -> PlanError.UnknownVariety(row.variety)
        else -> null
    }

    private fun lineFor(stock: SeedStock, plants: Int): PlanLine {
        val seeds = seedsToSow(plants, stock.germination)
        return PlanLine(
            variety = stock.variety,
            plants = plants,
            seedsToSow = seeds,
            packetsNeeded = packetsFor(seeds, stock.seedsPerPacket),
            packetsInStock = stock.packets,
        )
    }
}

private fun String.key(): String = trim().lowercase()
