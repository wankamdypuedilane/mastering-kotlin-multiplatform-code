package language

// Section 2.3: functions.

fun seedsToSow(plants: Int, ratePercent: Int = 80, spares: Int = 0): Int =
    (plants * 100 + ratePercent - 1) / ratePercent + spares

fun Int.ceilDiv(divisor: Int): Int = (this + divisor - 1) / divisor

val String.initials: String
    get() = split(" ").filter { it.isNotEmpty() }
        .joinToString("") { it.first().uppercase() }

data class Bed(val rows: Int, val plantsPerRow: Int)

infix fun Int.rowsOf(plantsPerRow: Int): Bed = Bed(this, plantsPerRow)

fun totalSeeds(vararg packets: Int): Int = packets.sum()

fun bedReport(bed: Bed): String {
    fun plural(n: Int, word: String) = if (n == 1) "$n $word" else "$n ${word}s"
    return "${plural(bed.rows, "row")} of ${plural(bed.plantsPerRow, "plant")}"
}
