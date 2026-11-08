// mendfield/core/model/src/commonMain/kotlin/mendfield/core/model/Event.kt
package mendfield.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

/**
 * One repair café session. Opening hours are wall-clock times in the
 * [timeZone] of the venue; an event never runs past midnight.
 */
data class Event(
    val id: String,
    val title: String,
    val venue: String,
    val date: LocalDate,
    val opensAt: LocalTime,
    val closesAt: LocalTime,
    val timeZone: TimeZone,
) {
    init {
        require(opensAt < closesAt) { "Event $id closes before it opens" }
    }
}
