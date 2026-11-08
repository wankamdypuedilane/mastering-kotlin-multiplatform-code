package labels.core

/** Result of reading a variety list: what parsed, and what did not. */
data class VarietyImport(val varieties: List<Variety>, val errors: List<String>)

/**
 * Reads lines `id,commonName,latinName,family,days,months,difficulty`,
 * where months are separated by spaces ("3 4 5"). A header line starting
 * with "id," and blank lines are skipped. Bad lines become errors.
 */
fun parseVarieties(csv: String): VarietyImport {
    val varieties = mutableListOf<Variety>()
    val errors = mutableListOf<String>()
    csv.lineSequence().forEachIndexed { index, raw ->
        val line = raw.trim()
        if (line.isEmpty() || line.startsWith("id,")) return@forEachIndexed
        val cells = line.split(',').map { it.trim() }
        val variety = cells.takeIf { it.size == 7 }?.let { c ->
            runCatching {
                Variety(
                    id = c[0], commonName = c[1],
                    latinName = c[2], family = c[3],
                    daysToMaturity = c[4].toInt(),
                    sowingMonths = c[5].split(' ').filter { it.isNotBlank() }
                        .map { it.toInt() }.toSet(),
                    difficulty = Difficulty.valueOf(c[6].uppercase()),
                )
            }.getOrNull()
        }
        if (variety != null) varieties += variety
        else errors += "Line ${index + 1}: cannot read \"$line\""
    }
    return VarietyImport(varieties, errors)
}
