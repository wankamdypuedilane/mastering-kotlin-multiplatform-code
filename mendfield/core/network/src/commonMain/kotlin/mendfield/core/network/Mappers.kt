// mendfield/core/network/src/commonMain/kotlin/mendfield/core/network/Mappers.kt
package mendfield.core.network

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.Outcome
import mendfield.core.model.OutcomeKind
import mendfield.core.model.RepairSlot
import mendfield.core.model.Skill

// DTO -> model. Parsing errors throw; MendfieldApi reports them as
// ApiError.Unexpected. On JS and Wasm, a region time zone such as
// "Europe/London" needs @js-joda/timezone to be loaded by the app.

fun EventDto.toModel() = Event(
    id = id, title = title, venue = venue,
    date = LocalDate.parse(date),
    opensAt = LocalTime.parse(opensAt),
    closesAt = LocalTime.parse(closesAt),
    timeZone = TimeZone.of(timeZone),
)

/** Skills this client does not know yet (a newer server) are skipped. */
fun FixerDto.toModel() = Fixer(
    id = id, displayName = displayName,
    skills = skills.mapNotNull { name ->
        Skill.entries.firstOrNull { it.name == name }
    }.toSet(),
)

fun ItemDto.toModel() = Item(
    id = id, eventId = eventId,
    category = ItemCategory.valueOf(category),
    description = description, visitorName = visitorName,
    registeredAt = Instant.parse(registeredAt),
)

fun RepairSlotDto.toModel() = RepairSlot(
    id = id, eventId = eventId, fixerId = fixerId,
    start = Instant.parse(start), end = Instant.parse(end), itemId = itemId,
)

fun OutcomeDto.toModel() = Outcome(
    id = id, itemId = itemId, kind = OutcomeKind.valueOf(kind),
    note = note, loggedAt = Instant.parse(loggedAt),
)

// Model -> DTO, for the operations of the outbox.

fun Item.toDto() = ItemDto(
    id = id, eventId = eventId, category = category.name,
    description = description, visitorName = visitorName,
    registeredAt = registeredAt.toString(),
)

fun Outcome.toDto() = OutcomeDto(
    id = id, itemId = itemId, kind = kind.name, note = note,
    loggedAt = loggedAt.toString(),
)
