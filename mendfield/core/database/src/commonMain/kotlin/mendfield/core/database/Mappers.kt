// mendfield/core/database/src/commonMain/kotlin/mendfield/core/database/Mappers.kt
package mendfield.core.database

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import mendfield.core.data.EventData
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.Outcome
import mendfield.core.model.RepairSlot

fun Event.toEntity() = EventEntity(id, title, venue, date.toString(),
    opensAt.toString(), closesAt.toString(), timeZone.id)

fun EventEntity.toModel() = Event(id, title, venue, LocalDate.parse(date),
    LocalTime.parse(opensAt), LocalTime.parse(closesAt), TimeZone.of(timeZone))

fun Fixer.toEntity() = FixerEntity(id, displayName, skills)
fun FixerEntity.toModel() = Fixer(id, displayName, skills)

fun Item.toEntity() =
    ItemEntity(id, eventId, category, description, visitorName, registeredAt)
fun ItemEntity.toModel() =
    Item(id, eventId, category, description, visitorName, registeredAt)

fun RepairSlot.toEntity() = SlotEntity(id, eventId, fixerId, start, end, itemId)
fun SlotEntity.toModel() = RepairSlot(id, eventId, fixerId, start, end, itemId)

fun Outcome.toEntity() = OutcomeEntity(id, itemId, kind, note, loggedAt)
fun OutcomeEntity.toModel() = Outcome(id, itemId, kind, note, loggedAt)

fun EventData.toRows() = EventRows(slots.map { it.toEntity() },
    items.map { it.toEntity() }, outcomes.map { it.toEntity() })

fun EventRows.toModel() = EventData(slots.map { it.toModel() },
    items.map { it.toModel() }, outcomes.map { it.toModel() })
