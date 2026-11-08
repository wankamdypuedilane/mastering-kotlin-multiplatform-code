// mendfield/core/model/src/commonMain/kotlin/mendfield/core/model/Item.kt
package mendfield.core.model

import kotlin.time.Instant

/**
 * The kind of a broken item: the skill a repair needs first, and the
 * skills that help when no fixer has that one.
 */
enum class ItemCategory(
    val primarySkill: Skill,
    val relatedSkills: Set<Skill>,
) {
    LAMP(Skill.ELECTRICAL, setOf(Skill.ELECTRONICS)),
    SMALL_APPLIANCE(Skill.ELECTRICAL, setOf(Skill.MECHANICAL)),
    AUDIO(Skill.ELECTRONICS, setOf(Skill.ELECTRICAL)),
    COMPUTER(Skill.COMPUTERS, setOf(Skill.ELECTRONICS)),
    BICYCLE(Skill.BICYCLES, setOf(Skill.MECHANICAL)),
    CLOCK(Skill.MECHANICAL, setOf(Skill.ELECTRONICS)),
    TOY(Skill.MECHANICAL, setOf(Skill.SEWING, Skill.ELECTRONICS)),
    CLOTHING(Skill.SEWING, emptySet()),
    FURNITURE(Skill.WOODWORK, emptySet()),
    TOOL(Skill.SHARPENING, setOf(Skill.MECHANICAL)),
}

/** A broken item a visitor brought (or pre-registered) for an event. */
data class Item(
    val id: String,
    val eventId: String,
    val category: ItemCategory,
    val description: String,
    val visitorName: String,
    val registeredAt: Instant,
)
