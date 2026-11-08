// mendfield/core/domain/src/commonMain/kotlin/mendfield/core/domain/Triage.kt
package mendfield.core.domain

import mendfield.core.model.Fixer
import mendfield.core.model.ItemCategory
import mendfield.core.model.RepairSlot

/** How well a fixer's skills fit an item. Declared best first. */
enum class SkillMatch { PRIMARY, RELATED }

data class FixerSuggestion(
    val fixer: Fixer,
    val match: SkillMatch,
    /** Items already assigned to this fixer at the event. */
    val load: Int,
)

/**
 * The triage rules. A fixer qualifies with the category's primary skill or,
 * failing that, one of its related skills. Ranking, best first: primary
 * before related, lighter load, more related skills, then name and id so
 * that the order is always the same.
 */
fun suggestFixers(
    category: ItemCategory,
    fixers: List<Fixer>,
    eventSlots: List<RepairSlot>,
): List<FixerSuggestion> {
    val loads = eventSlots.filterNot { it.isOpen }
        .groupingBy { it.fixerId }.eachCount()
    return fixers
        .mapNotNull { fixer ->
            fixer.matchFor(category)?.let { match ->
                FixerSuggestion(fixer, match, loads[fixer.id] ?: 0)
            }
        }
        .sortedWith(
            compareBy<FixerSuggestion>({ it.match }, { it.load })
                .thenByDescending { it.fixer.relatedSkillCount(category) }
                .thenBy { it.fixer.displayName }.thenBy { it.fixer.id },
        )
}

private fun Fixer.relatedSkillCount(category: ItemCategory): Int =
    skills.count { it in category.relatedSkills }

private fun Fixer.matchFor(category: ItemCategory): SkillMatch? = when {
    category.primarySkill in skills -> SkillMatch.PRIMARY
    skills.any { it in category.relatedSkills } -> SkillMatch.RELATED
    else -> null
}
