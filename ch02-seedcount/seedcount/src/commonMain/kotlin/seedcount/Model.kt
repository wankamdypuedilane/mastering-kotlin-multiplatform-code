package seedcount

/** Packets of one variety in the seed box. */
data class SeedStock(
    val variety: String,
    val packets: Int,
    val seedsPerPacket: Int,
    val germination: GerminationRate,
) {
    init {
        require(packets >= 0) { "packets cannot be negative" }
        require(seedsPerPacket > 0) { "a packet holds at least one seed" }
    }
}

/** One line of the planting plan: how many plants you want. */
data class PlantingRow(val variety: String, val plants: Int)

/** The computed need for one variety. */
data class PlanLine(
    val variety: String,
    val plants: Int,
    val seedsToSow: Int,
    val packetsNeeded: Int,
    val packetsInStock: Int,
) {
    val shortfall: Int get() = maxOf(0, packetsNeeded - packetsInStock)
}
