package seedcount

fun rate(percent: Int): GerminationRate =
    GerminationRate.of(percent) ?: error("bad test rate $percent")

val sampleStock = listOf(
    SeedStock("Larkmoor Red", packets = 2, seedsPerPacket = 25, rate(80)),
    SeedStock("Scarlet Runner", packets = 1, seedsPerPacket = 30, rate(75)),
    SeedStock("Winter Kale", packets = 3, seedsPerPacket = 100, rate(90)),
)
