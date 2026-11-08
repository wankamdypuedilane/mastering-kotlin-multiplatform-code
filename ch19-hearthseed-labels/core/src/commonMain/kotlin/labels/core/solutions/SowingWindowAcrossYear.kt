package labels.core.solutions

import labels.core.sowingWindow

/**
 * Exercise 19.2: like [sowingWindow], but a run that ends in December and
 * one that starts in January become a single range: {11, 12, 1} -> "Nov–Jan".
 */
fun sowingWindowAcrossYear(months: Set<Int>): String {
    if (months.size == 12) return "all year"
    if (12 !in months || 1 !in months) return sowingWindow(months)
    // Rotate the calendar so that it starts just after the first gap.
    val firstGap = (1..12).first { it !in months }
    val order = (0 until 12).map { (firstGap - 1 + it) % 12 + 1 }
    val runs = mutableListOf<List<Int>>()
    var current = mutableListOf<Int>()
    for (m in order) {
        if (m in months) current += m
        else if (current.isNotEmpty()) {
            runs += current
            current = mutableListOf()
        }
    }
    if (current.isNotEmpty()) runs += current
    return runs.joinToString(", ") { run ->
        val first = sowingWindow(setOf(run.first()))
        if (run.size == 1) first
        else "$first–${sowingWindow(setOf(run.last()))}"
    }
}
