// mendfield/feature/outcomes/src/commonMain/kotlin/mendfield/feature/outcomes/LogOutcomeViewModel.kt
package mendfield.feature.outcomes

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mendfield.core.data.ItemHistory
import mendfield.core.data.ItemLimits
import mendfield.core.data.OutcomeRepository
import mendfield.core.data.WriteResult
import mendfield.core.model.OutcomeKind
import mendfield.core.ui.clockTime
import mendfield.core.ui.label

@Immutable
data class ItemSummary(
    val title: String,   // "Lamp · Desk lamp flickers"
    val visitor: String,
    val slot: String,    // "Maud Okafor, 10:00–10:30" or "No slot yet"
    val history: List<String>, // "10:40 Partly fixed: needs a part"
)

sealed interface ItemStatus {
    data object Loading : ItemStatus
    data object Missing : ItemStatus
    data class Ready(val summary: ItemSummary) : ItemStatus
}

@Immutable
data class LogOutcomeUiState(
    val item: ItemStatus = ItemStatus.Loading,
    val kind: OutcomeKind? = null,
    val note: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
) {
    val noteTooLong: Boolean get() = note.trim().length > ItemLimits.NOTE
    val canSave: Boolean
        get() = item is ItemStatus.Ready && kind != null && !noteTooLong &&
            !isSaving && !saved
}

class LogOutcomeViewModel(
    private val itemId: String,
    private val outcomes: OutcomeRepository,
) : ViewModel() {
    private val input = MutableStateFlow(LogOutcomeUiState())

    val state: StateFlow<LogOutcomeUiState> =
        combine(outcomes.itemHistory(itemId), input) { history, form ->
            form.copy(item = history?.let { ItemStatus.Ready(it.toSummary()) }
                ?: ItemStatus.Missing)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            LogOutcomeUiState())

    fun onKind(kind: OutcomeKind) = input.update { it.copy(kind = kind, error = null) }
    fun onNote(note: String) = input.update { it.copy(note = note) }

    fun save() {
        // Read the input, not `state`: state may not have caught up yet.
        val form = input.value
        val kind = form.kind ?: return
        if (form.noteTooLong || form.isSaving || form.saved) return
        input.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (val r = outcomes.logOutcome(itemId, kind, input.value.note)) {
                is WriteResult.Saved -> input.update { it.copy(isSaving = false, saved = true) }
                is WriteResult.Refused ->
                    input.update { it.copy(isSaving = false, error = r.reason) }
            }
        }
    }
}

internal fun ItemHistory.toSummary(): ItemSummary {
    val zone = event?.timeZone
    val slot = slot // a local copy can be smart-cast
    return ItemSummary(
        title = "${item.category.label()} · ${item.description}",
        visitor = item.visitorName,
        slot = if (slot == null || zone == null) "No slot yet"
        else "${fixer?.displayName ?: slot.fixerId}, " +
            "${slot.start.clockTime(zone)}–${slot.end.clockTime(zone)}",
        history = outcomes.map {
            val time = zone?.let { z -> it.loggedAt.clockTime(z) }.orEmpty()
            val note = if (it.note.isBlank()) "" else ": ${it.note}"
            "$time ${it.kind.label()}$note".trim()
        },
    )
}
