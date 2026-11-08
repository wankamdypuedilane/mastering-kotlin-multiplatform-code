package language

// Section 2.2: control flow as expressions.

fun sizeLabel(plants: Int): String =
    if (plants < 10) "small" else if (plants < 100) "medium" else "large"

fun season(month: Int): String = when (month) {
    12, 1, 2 -> "winter"
    in 3..5 -> "spring"
    in 6..8 -> "summer"
    in 9..11 -> "autumn"
    else -> throw IllegalArgumentException("No month $month")
}

fun advice(ratePercent: Int, ageYears: Int): String = when {
    ratePercent >= 85 -> "sow as usual"
    ratePercent >= 60 && ageYears <= 2 -> "sow a little thicker"
    else -> "run a germination test first"
}

fun ranges(): List<String> = listOf(
    (1..5).joinToString(), // 1, 2, 3, 4, 5
    (1..<5).joinToString(), // 1, 2, 3, 4
    (10 downTo 0 step 5).joinToString(), // 10, 5, 0
    ('a'..'e').joinToString(""), // abcde
)

fun firstFullTray(trays: List<Int>, capacity: Int): Int {
    for ((index, seeds) in trays.withIndex()) {
        if (seeds == capacity) return index
    }
    return -1
}

fun firstEmptyCell(grid: List<List<Int>>): Pair<Int, Int>? {
    var found: Pair<Int, Int>? = null
    rows@ for ((row, cells) in grid.withIndex()) {
        for ((column, seeds) in cells.withIndex()) {
            if (seeds == 0) {
                found = row to column
                break@rows
            }
        }
    }
    return found
}

fun sownUntilFull(batches: List<List<Int>>, capacity: Int): Int {
    var sown = 0
    for (batch in batches) {
        batch.forEach { seeds ->
            if (sown + seeds > capacity) break // leaves the for loop
            sown += seeds
        }
    }
    return sown
}
