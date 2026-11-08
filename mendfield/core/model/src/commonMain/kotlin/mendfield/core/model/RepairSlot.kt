// mendfield/core/model/src/commonMain/kotlin/mendfield/core/model/RepairSlot.kt
package mendfield.core.model

import kotlin.time.Instant

/**
 * A fixer's time at an event, from [start] (inclusive) to [end] (exclusive).
 * An open slot has no [itemId]; an assigned slot holds exactly one item.
 */
data class RepairSlot(
    val id: String,
    val eventId: String,
    val fixerId: String,
    val start: Instant,
    val end: Instant,
    val itemId: String? = null,
) {
    init {
        require(start < end) { "Slot $id ends before it starts" }
    }

    val isOpen: Boolean get() = itemId == null
}
