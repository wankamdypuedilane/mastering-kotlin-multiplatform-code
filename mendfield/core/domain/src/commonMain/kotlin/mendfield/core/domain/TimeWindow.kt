// mendfield/core/domain/src/commonMain/kotlin/mendfield/core/domain/TimeWindow.kt
package mendfield.core.domain

import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import mendfield.core.model.Event
import mendfield.core.model.RepairSlot

/** A half-open interval of instants: [start] inclusive, [end] exclusive. */
data class TimeWindow(val start: Instant, val end: Instant) {
    init {
        require(start < end) { "Empty window: $start..$end" }
    }

    val duration: Duration get() = end - start

    /** Touching windows (10:00-10:30 and 10:30-11:00) do not overlap. */
    fun overlaps(other: TimeWindow): Boolean =
        start < other.end && other.start < end

    operator fun contains(other: TimeWindow): Boolean =
        other.start >= start && other.end <= end
}

val RepairSlot.window: TimeWindow get() = TimeWindow(start, end)

/** The opening hours of the event as instants. */
fun Event.hours(): TimeWindow = window(opensAt, closesAt)!!

/**
 * The part of the event between two wall-clock times of its day, converted
 * in the event's time zone; null when that part is empty.
 */
fun Event.window(from: LocalTime, until: LocalTime): TimeWindow? {
    val start = maxOf(from, opensAt)
    val end = minOf(until, closesAt)
    if (start >= end) return null
    val startInstant = date.atTime(start).toInstant(timeZone)
    val endInstant = date.atTime(end).toInstant(timeZone)
    // A daylight-saving gap can swallow a very short window.
    if (startInstant >= endInstant) return null
    return TimeWindow(startInstant, endInstant)
}
