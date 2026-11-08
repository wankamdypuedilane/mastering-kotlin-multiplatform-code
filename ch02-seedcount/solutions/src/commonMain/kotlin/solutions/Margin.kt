package solutions

import seedcount.GerminationRate
import seedcount.seedsToSow

// Exercise 2.1: a safety margin on top of the expected losses.
fun seedsWithMargin(
    plants: Int,
    rate: GerminationRate,
    marginPercent: Int = 10,
): Int {
    require(marginPercent >= 0) { "margin cannot be negative" }
    val base = seedsToSow(plants, rate)
    val extra = (base * marginPercent + 99) / 100 // round up
    return base + extra
}
