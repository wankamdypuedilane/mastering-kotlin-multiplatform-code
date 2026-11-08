// mendfield/feature/intake/src/commonMain/kotlin/mendfield/feature/intake/IntakeViewModel.kt
package mendfield.feature.intake

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mendfield.core.data.DraftProblem
import mendfield.core.data.EventRepository
import mendfield.core.data.IntakeRepository
import mendfield.core.data.WriteResult
import mendfield.core.data.draftProblems
import mendfield.core.data.fixerSuggestions
import mendfield.core.model.ItemCategory
import mendfield.core.ui.label

/** What the organizer typed: the part of the state that survives errors. */
@Immutable
data class IntakeForm(
    val category: ItemCategory? = null,
    val description: String = "",
    val visitorName: String = "",
    val showProblems: Boolean = false, // only after the first Save
)

/** "Maud Okafor: best fit, 1 booked" */
@Immutable
data class TriageHint(val fixerId: String, val text: String)

@Immutable
data class IntakeUiState(
    val eventTitle: String = "",
    val form: IntakeForm = IntakeForm(),
    val problems: Set<DraftProblem> = emptySet(),
    val hints: List<TriageHint> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    /** Set once: the screen navigates away and the item is on the board. */
    val savedItemId: String? = null,
) {
    val canSave: Boolean get() = !isSaving && savedItemId == null
}

class IntakeViewModel(
    private val eventId: String,
    events: EventRepository,
    private val intake: IntakeRepository,
    compute: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val form = MutableStateFlow(IntakeForm())
    private val progress = MutableStateFlow(Progress())

    private data class Progress(
        val isSaving: Boolean = false,
        val error: String? = null,
        val savedItemId: String? = null,
    )

    /** The triage of :core:domain, live: it changes with the category. */
    private val hints = combine(
        events.board(eventId),
        form.map { it.category }.distinctUntilChanged(),
    ) { board, category ->
        if (board == null || category == null) emptyList()
        else board.fixerSuggestions(category).take(3).map {
            TriageHint(it.fixer.id,
                "${it.fixer.displayName}: ${it.match.label()}, ${it.load} booked")
        }
    }

    val state: StateFlow<IntakeUiState> = combine(
        events.board(eventId).map { it?.event?.title.orEmpty() }.distinctUntilChanged(),
        form, hints, progress,
    ) { title, f, h, p ->
        IntakeUiState(
            eventTitle = title,
            form = f,
            problems = if (f.showProblems) draftProblems(f.description, f.visitorName)
            else emptySet(),
            hints = h,
            isSaving = p.isSaving,
            error = p.error,
            savedItemId = p.savedItemId,
        )
    }.flowOn(compute).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
        IntakeUiState())

    fun onCategory(category: ItemCategory) = form.update { it.copy(category = category) }
    fun onDescription(text: String) = form.update { it.copy(description = text) }
    fun onVisitorName(text: String) = form.update { it.copy(visitorName = text) }

    fun save() {
        val f = form.value
        val category = f.category
        if (progress.value.isSaving) return
        form.update { it.copy(showProblems = true) }
        if (category == null) {
            progress.update { it.copy(error = "Choose a category.") }
            return
        }
        if (draftProblems(f.description, f.visitorName).isNotEmpty()) return
        progress.value = Progress(isSaving = true)
        viewModelScope.launch {
            // Never waits for the network: the item goes to the device and
            // the outbox, so intake works the same offline.
            val result = intake.registerItem(eventId, category, f.description,
                f.visitorName)
            progress.value = when (result) {
                is WriteResult.Saved -> Progress(savedItemId = result.value.id)
                is WriteResult.Refused -> Progress(error = result.reason)
            }
        }
    }
}
