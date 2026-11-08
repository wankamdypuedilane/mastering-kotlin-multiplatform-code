// mendfield/feature/events/src/commonMain/kotlin/mendfield/feature/events/BoardContent.kt
package mendfield.feature.events

import androidx.compose.runtime.Immutable
import kotlin.time.Instant
import mendfield.core.data.EventBoard
import mendfield.core.data.slotSuggestions
import mendfield.core.model.Item
import mendfield.core.ui.clockTime
import mendfield.core.ui.label

@Immutable
data class BoardCounts(
    val registered: Int,
    val waiting: Int,
    val scheduled: Int,
    val withOutcome: Int,
)

@Immutable
data class WaitingRow(
    val itemId: String,
    val title: String,   // "Lamp · Desk lamp flickers"
    val visitor: String,
    val isSelected: Boolean,
)

@Immutable
data class SuggestionRow(val slotId: String, val text: String)

/** The waiting item the organizer tapped, with the triage's proposals. */
@Immutable
data class Selection(
    val itemId: String,
    val title: String,
    val suggestions: List<SuggestionRow>,
)

@Immutable
data class SlotRow(
    val slotId: String,
    val time: String,        // "10:30–11:00"
    val itemId: String?,
    val itemText: String?,   // "Desk lamp flickers (Ada Mensah)"
    val outcome: String?,    // "Fixed"
)

@Immutable
data class FixerSchedule(
    val fixerId: String,
    val name: String,
    val skills: String,
    val slots: List<SlotRow>,
)

@Immutable
data class BoardContent(
    val counts: BoardCounts,
    val waiting: List<WaitingRow>,
    val selection: Selection?,
    val schedule: List<FixerSchedule>,
)

/**
 * Everything the board shows, computed once per change of the data, off the
 * main thread (the ViewModel runs it with flowOn): composition only reads
 * prepared rows and counts, it never filters or sorts.
 */
fun boardContent(board: EventBoard, selectedItemId: String?, now: Instant): BoardContent {
    val zone = board.event.timeZone
    val itemsById = board.items.associateBy { it.id }
    val scheduled = board.slots.mapNotNull { it.itemId }.toSet()
    val latestOutcome = board.outcomes.groupBy { it.itemId }
        .mapValues { (_, list) -> list.last() } // the cache sorts by loggedAt
    val waitingItems = board.items.filter { it.id !in scheduled }
    val selected = waitingItems.firstOrNull { it.id == selectedItemId }

    return BoardContent(
        counts = BoardCounts(
            registered = board.items.size,
            waiting = waitingItems.size,
            scheduled = scheduled.size,
            withOutcome = latestOutcome.size,
        ),
        waiting = waitingItems.map {
            WaitingRow(it.id, it.title(), it.visitorName, it.id == selected?.id)
        },
        selection = selected?.let { item ->
            val fixers = board.fixers.associateBy { it.id }
            // Only slots that have not started yet: [now] comes from a Clock.
            Selection(item.id, item.title(), board.slotSuggestions(item, now).map { s ->
                val name = fixers[s.slot.fixerId]?.displayName ?: s.slot.fixerId
                SuggestionRow(s.slot.id,
                    "$name at ${s.slot.start.clockTime(zone)} " +
                        "(${s.suggestion.match.label()}, ${s.suggestion.load} booked)")
            })
        },
        schedule = board.fixers.map { fixer ->
            FixerSchedule(
                fixerId = fixer.id,
                name = fixer.displayName,
                skills = fixer.skills.sorted().joinToString { it.label() },
                slots = board.slots.filter { it.fixerId == fixer.id }.map { slot ->
                    val item = slot.itemId?.let(itemsById::get)
                    SlotRow(
                        slotId = slot.id,
                        time = "${slot.start.clockTime(zone)}–${slot.end.clockTime(zone)}",
                        itemId = slot.itemId,
                        itemText = item?.let { "${it.description} (${it.visitorName})" },
                        outcome = item?.let { latestOutcome[it.id]?.kind?.label() },
                    )
                },
            )
        }.filter { it.slots.isNotEmpty() },
    )
}

private fun Item.title() = "${category.label()} · $description"
