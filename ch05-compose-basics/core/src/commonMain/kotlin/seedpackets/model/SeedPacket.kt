package seedpackets.model

/** Plant families used to group packets in the gallery. */
enum class Family { LEGUMES, NIGHTSHADES, SQUASHES, GREENS }

/** One packet of seed offered by the Larkmoor Commons Seed Library. */
data class SeedPacket(
    val id: String,
    val name: String,
    val family: Family,
    val daysToMaturity: Int,
    val packetsInStock: Int,
) {
    init {
        require(daysToMaturity > 0) { "daysToMaturity must be positive" }
        require(packetsInStock >= 0) { "packetsInStock cannot be negative" }
    }

    val isAvailable: Boolean get() = packetsInStock > 0
}

/** Keeps the packets of [family], or all packets when [family] is null. */
fun List<SeedPacket>.inFamily(family: Family?): List<SeedPacket> =
    if (family == null) this else filter { it.family == family }

/** A fixed, fictional catalog for the gallery and its tests. */
object SampleCatalog {
    val packets: List<SeedPacket> = listOf(
        SeedPacket("bean-runner", "Larkmoor Runner", Family.LEGUMES, 70, 12),
        SeedPacket("tomato-ember", "Ember Cherry", Family.NIGHTSHADES, 65, 4),
        SeedPacket("squash-moon", "Pale Moon", Family.SQUASHES, 95, 0),
        SeedPacket("lettuce-copper", "Copper Leaf", Family.GREENS, 45, 9),
        SeedPacket("pea-ladder", "Sugar Ladder", Family.LEGUMES, 60, 7),
        SeedPacket("kale-ash", "Ash Hollow", Family.GREENS, 55, 3),
    )
}
