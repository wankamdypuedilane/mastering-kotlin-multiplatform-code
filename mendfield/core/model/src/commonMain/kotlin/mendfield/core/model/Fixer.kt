// mendfield/core/model/src/commonMain/kotlin/mendfield/core/model/Fixer.kt
package mendfield.core.model

/** What a volunteer fixer knows how to repair. */
enum class Skill {
    ELECTRICAL, ELECTRONICS, COMPUTERS, MECHANICAL,
    BICYCLES, SEWING, WOODWORK, SHARPENING,
}

data class Fixer(
    val id: String,
    val displayName: String,
    val skills: Set<Skill>,
)
