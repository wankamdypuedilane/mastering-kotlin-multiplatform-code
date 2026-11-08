package seedcount

/** Integer division that rounds up: you cannot sow half a seed. */
internal fun Int.ceilDiv(divisor: Int): Int {
    require(this >= 0 && divisor > 0) { "cannot ceilDiv $this by $divisor" }
    return (this + divisor - 1) / divisor
}

/** Seeds to sow so that, on average, [plants] seedlings come up. */
fun seedsToSow(plants: Int, rate: GerminationRate): Int =
    (plants * 100).ceilDiv(rate.percent)

fun packetsFor(seeds: Int, seedsPerPacket: Int): Int =
    seeds.ceilDiv(seedsPerPacket)
