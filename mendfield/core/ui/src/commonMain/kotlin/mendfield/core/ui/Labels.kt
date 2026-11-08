// mendfield/core/ui/src/commonMain/kotlin/mendfield/core/ui/Labels.kt
package mendfield.core.ui

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mendfield.core.domain.SkillMatch
import mendfield.core.model.ItemCategory
import mendfield.core.model.OutcomeKind
import mendfield.core.model.Skill

// Words for the screens, in one place (a translation would start here).

fun ItemCategory.label(): String = when (this) {
    ItemCategory.LAMP -> "Lamp"
    ItemCategory.SMALL_APPLIANCE -> "Small appliance"
    ItemCategory.AUDIO -> "Audio"
    ItemCategory.COMPUTER -> "Computer"
    ItemCategory.BICYCLE -> "Bicycle"
    ItemCategory.CLOCK -> "Clock"
    ItemCategory.TOY -> "Toy"
    ItemCategory.CLOTHING -> "Clothing"
    ItemCategory.FURNITURE -> "Furniture"
    ItemCategory.TOOL -> "Tool"
}

fun OutcomeKind.label(): String = when (this) {
    OutcomeKind.FIXED -> "Fixed"
    OutcomeKind.PARTIAL -> "Partly fixed"
    OutcomeKind.NOT_FIXED -> "Not fixed"
    OutcomeKind.ADVICE -> "Advice only"
}

fun Skill.label(): String =
    name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

fun SkillMatch.label(): String = when (this) {
    SkillMatch.PRIMARY -> "best fit"
    SkillMatch.RELATED -> "related skill"
}

/** "10:30": a wall-clock time at the venue, whatever the device's zone. */
fun Instant.clockTime(zone: TimeZone): String =
    toLocalDateTime(zone).time.hhmm()

fun LocalTime.hhmm(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

/** "Sat 21 Nov 2026". */
fun LocalDate.label(): String {
    val weekday = dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val mon = month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$weekday $day $mon $year"
}
