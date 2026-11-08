// mendfield/server/src/main/kotlin/mendfield/server/Mappers.kt
package mendfield.server

import kotlin.time.Instant
import mendfield.core.domain.OutcomeStats
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.Outcome
import mendfield.core.model.OutcomeKind
import mendfield.core.model.RepairSlot

fun Event.toDto() = EventDto(
    id, title, venue, date.toString(), opensAt.toString(), closesAt.toString(),
    timeZone.id,
)

fun Fixer.toDto() =
    FixerDto(id, displayName, skills.sortedBy { it.ordinal }.map { it.name })

fun Item.toDto() = ItemDto(
    id, eventId, category.name, description, visitorName,
    registeredAt.toString(),
)

fun RepairSlot.toDto() =
    RepairSlotDto(id, eventId, fixerId, start.toString(), end.toString(),
        itemId)

fun Outcome.toDto() =
    OutcomeDto(id, itemId, kind.name, note, loggedAt.toString())

@JvmName("slotDtos") fun List<RepairSlot>.toDtos() = map { it.toDto() }
@JvmName("itemDtos") fun List<Item>.toDtos() = map { it.toDto() }
@JvmName("outcomeDtos") fun List<Outcome>.toDtos() = map { it.toDto() }

fun OutcomeStats.toDto(eventId: String) = StatsDto(
    eventId = eventId,
    registered = registered,
    withOutcome = withOutcome,
    counts = counts.mapKeys { it.key.name },
    repairRatePercent = repairRatePercent,
)

// Input parsing: every failure is a 400 (or a REJECTED sync operation).

private val idPattern = Regex("[A-Za-z0-9._-]{1,64}")

fun validId(id: String, what: String): String =
    id.also {
        if (!idPattern.matches(it)) throw InvalidInput("Invalid $what id: $it")
    }

fun parseCategory(name: String): ItemCategory =
    ItemCategory.entries.firstOrNull { it.name == name }
        ?: throw InvalidInput("Unknown category: $name")

fun parseKind(name: String): OutcomeKind =
    OutcomeKind.entries.firstOrNull { it.name == name }
        ?: throw InvalidInput("Unknown outcome kind: $name")

fun parseInstant(text: String, what: String): Instant =
    try {
        Instant.parse(text)
    } catch (_: IllegalArgumentException) {
        throw InvalidInput("Invalid $what: $text")
    }

fun requireText(text: String, what: String, maxLength: Int): String {
    val trimmed = text.trim()
    if (trimmed.isEmpty() || trimmed.length > maxLength) {
        throw InvalidInput("$what must have 1 to $maxLength characters")
    }
    return trimmed
}
