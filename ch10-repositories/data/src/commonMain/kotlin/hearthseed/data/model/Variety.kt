package hearthseed.data.model

/** How hard a variety is to grow and to save seed from. */
enum class Difficulty { EASY, MEDIUM, HARD }

/** A domain model: valid by construction, free of JSON concerns. */
data class Variety(
    val id: String,
    val commonName: String,
    val latinName: String,
    val family: String,
    val daysToMaturity: Int,
    val sowingMonths: Set<Int>,
    val difficulty: Difficulty,
    val description: String,
)
