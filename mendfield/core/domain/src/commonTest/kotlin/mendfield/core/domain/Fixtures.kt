// mendfield/core/domain/src/commonTest/kotlin/mendfield/core/domain/Fixtures.kt
package mendfield.core.domain

import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.Skill

/**
 * Test data. A fixed UTC+01:00 offset keeps the tests identical on every
 * target: region ids such as "Europe/London" need a time-zone database,
 * which JS and Wasm load only on request (see the JVM DST tests).
 */
internal object Fixtures {
    val zone = UtcOffset(hours = 1).asTimeZone()
    val event = Event(
        id = "evt-t", title = "Test café", venue = "Hall",
        date = LocalDate(2026, 10, 17),
        opensAt = LocalTime(10, 0), closesAt = LocalTime(13, 0),
        timeZone = zone,
    )

    /** An instant on the event's day, from a wall-clock time. */
    fun at(hour: Int, minute: Int = 0): Instant =
        event.date.atTime(LocalTime(hour, minute)).toInstant(zone)

    val maud = Fixer("fx-1", "Maud", setOf(Skill.ELECTRICAL, Skill.ELECTRONICS))
    val tomasz = Fixer("fx-2", "Tomasz", setOf(Skill.BICYCLES, Skill.MECHANICAL))
    val ines = Fixer("fx-3", "Ines", setOf(Skill.SEWING))
    val callum = Fixer("fx-4", "Callum", setOf(Skill.ELECTRICAL, Skill.MECHANICAL))

    fun grid(fixer: Fixer) = slotGrid(event, fixer.id, 30.minutes)

    fun item(
        id: String,
        category: ItemCategory,
        visitor: String = "Visitor $id",
        registeredMinute: Int = 0,
    ) = Item(
        id = id, eventId = event.id, category = category,
        description = "Broken $category", visitorName = visitor,
        registeredAt = at(9, 0) + registeredMinute.minutes,
    )
}
