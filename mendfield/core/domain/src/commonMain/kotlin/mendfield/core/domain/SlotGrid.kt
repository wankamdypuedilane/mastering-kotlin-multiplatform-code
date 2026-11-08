// mendfield/core/domain/src/commonMain/kotlin/mendfield/core/domain/SlotGrid.kt
package mendfield.core.domain

import kotlin.time.Duration
import kotlinx.datetime.LocalTime
import mendfield.core.model.Event
import mendfield.core.model.RepairSlot

/** Slot ids are stable: the same event, fixer and position give the same id. */
fun slotId(eventId: String, fixerId: String, number: Int): String =
    "$eventId.$fixerId.$number"

/**
 * Back-to-back open slots of [length] for one fixer, from [from] to [until]
 * (wall-clock times, clipped to the event's hours). A remainder shorter than
 * [length] at the end is left free.
 */
fun slotGrid(
    event: Event,
    fixerId: String,
    length: Duration,
    from: LocalTime = event.opensAt,
    until: LocalTime = event.closesAt,
): List<RepairSlot> {
    require(length.isPositive()) { "Slot length must be positive: $length" }
    val window = event.window(from, until) ?: return emptyList()
    return generateSequence(window.start) { it + length }
        .takeWhile { it + length <= window.end }
        .mapIndexed { index, start ->
            RepairSlot(
                id = slotId(event.id, fixerId, index + 1),
                eventId = event.id,
                fixerId = fixerId,
                start = start,
                end = start + length,
            )
        }
        .toList()
}
