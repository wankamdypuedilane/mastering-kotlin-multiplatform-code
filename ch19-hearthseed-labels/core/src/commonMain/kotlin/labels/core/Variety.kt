package labels.core

enum class Difficulty { EASY, MEDIUM, HARD }

/** A seed variety, as in the Hearthseed catalog (PROJECTS.md). */
data class Variety(
    val id: String,
    val commonName: String,
    val latinName: String,
    val family: String,
    val daysToMaturity: Int,
    val sowingMonths: Set<Int>,
    val difficulty: Difficulty,
) {
    init {
        require(daysToMaturity > 0) { "daysToMaturity must be positive" }
        require(sowingMonths.all { it in 1..12 }) { "months are 1..12" }
    }
}

/** A small bundled list, so the app works without the server. */
object SampleVarieties {
    val all: List<Variety> = listOf(
        Variety("var-001", "Larkmoor Ember tomato", "Solanum lycopersicum",
            "Solanaceae", 72, setOf(3, 4), Difficulty.MEDIUM),
        Variety("var-009", "Wren's Runner bean", "Phaseolus coccineus",
            "Fabaceae", 70, setOf(5, 6), Difficulty.EASY),
        Variety("var-011", "Milestone broad bean", "Vicia faba",
            "Fabaceae", 90, setOf(2, 3, 10, 11), Difficulty.EASY),
        Variety("var-023", "Firebrand radish", "Raphanus sativus",
            "Brassicaceae", 28, setOf(3, 4, 5, 6, 7, 8, 9), Difficulty.EASY),
        Variety("var-026", "Sandbank carrot", "Daucus carota",
            "Apiaceae", 75, setOf(3, 4, 5, 6), Difficulty.MEDIUM),
        Variety("var-037", "Kingsway leek", "Allium porrum",
            "Amaryllidaceae", 150, setOf(2, 3, 4), Difficulty.MEDIUM),
    )
}
