package labels.core

private val MONTHS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

/**
 * Formats sowing months as ranges of consecutive months:
 * {2, 3, 10, 11} -> "Feb–Mar, Oct–Nov"; {5} -> "May".
 */
fun sowingWindow(months: Set<Int>): String {
    if (months.isEmpty()) return "any time"
    val sorted = months.sorted()
    val runs = mutableListOf<IntRange>()
    var start = sorted.first()
    var end = start
    for (m in sorted.drop(1)) {
        if (m == end + 1) end = m else {
            runs += start..end
            start = m
            end = m
        }
    }
    runs += start..end
    return runs.joinToString(", ") { r ->
        if (r.first == r.last) MONTHS[r.first - 1]
        else "${MONTHS[r.first - 1]}–${MONTHS[r.last - 1]}"
    }
}
