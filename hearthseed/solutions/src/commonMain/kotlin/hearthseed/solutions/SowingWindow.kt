// hearthseed/solutions/src/commonMain/kotlin/hearthseed/solutions/SowingWindow.kt
package hearthseed.solutions

import hearthseed.core.model.Variety

private val names = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** Exercise 11.1: {2, 5, 6, 7} -> "Feb, May–Jul" (runs become ranges). */
fun Variety.sowingWindow(): String {
    val runs = mutableListOf<IntRange>()
    for (month in sowingMonths.sorted()) {
        val last = runs.lastOrNull()
        if (last != null && month == last.last + 1) {
            runs[runs.lastIndex] = last.first..month // extend the current run
        } else {
            runs += month..month
        }
    }
    return runs.joinToString(", ") { run ->
        if (run.first == run.last) names[run.first - 1]
        else "${names[run.first - 1]}–${names[run.last - 1]}"
    }
}
