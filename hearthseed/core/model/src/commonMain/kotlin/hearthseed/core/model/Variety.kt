// hearthseed/core/model/src/commonMain/kotlin/hearthseed/core/model/Variety.kt
package hearthseed.core.model

/** How hard a variety is to grow and to save seed from. */
enum class Difficulty { EASY, MEDIUM, HARD }

/** An open-pollinated variety in the library's catalog. */
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
