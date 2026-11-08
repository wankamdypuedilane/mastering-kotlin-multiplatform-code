// mendfield/feature/events/src/commonMain/kotlin/mendfield/feature/events/EventBoardViewModel.kt
package mendfield.feature.events

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mendfield.core.data.DataError
import mendfield.core.data.DataResult
import mendfield.core.data.EventRepository
import mendfield.core.data.IntakeRepository
import mendfield.core.data.WriteResult
import mendfield.core.ui.hhmm
import mendfield.core.ui.label

/** Exactly one of three situations; a failed refresh keeps the board. */
sealed interface BoardStatus {
    data object Loading : BoardStatus
    data class Missing(val error: DataError?) : BoardStatus
    data class Ready(val content: BoardContent) : BoardStatus
}

@Immutable
data class BoardUiState(
    val title: String = "",
    val subtitle: String = "",
    val status: BoardStatus = BoardStatus.Loading,
    val isRefreshing: Boolean = false,
    val refreshError: DataError? = null,
    val message: String? = null, // one-off: "The slot already holds..."
)

class EventBoardViewModel(
    private val eventId: String,
    private val events: EventRepository,
    private val intake: IntakeRepository,
    private val clock: Clock = Clock.System,
    private val compute: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val selected = MutableStateFlow<String?>(null)
    private val refreshState = MutableStateFlow(RefreshState())
    private val message = MutableStateFlow<String?>(null)

    val state: StateFlow<BoardUiState> = combine(
        events.board(eventId), selected, refreshState, message,
    ) { board, selectedId, refresh, msg ->
        val status = when {
            board != null ->
                BoardStatus.Ready(boardContent(board, selectedId, clock.now()))
            refresh.isRefreshing -> BoardStatus.Loading
            else -> BoardStatus.Missing(refresh.error)
        }
        BoardUiState(
            title = board?.event?.title.orEmpty(),
            subtitle = board?.event?.let {
                "${it.date.label()}, ${it.opensAt.hhmm()}–${it.closesAt.hhmm()}"
            }.orEmpty(),
            status = status,
            isRefreshing = refresh.isRefreshing,
            refreshError = refresh.error,
            message = msg,
        )
    }
        .flowOn(compute) // boardContent groups and formats: not on main
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            BoardUiState(status = BoardStatus.Loading, isRefreshing = true))

    init { refresh() }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.value = RefreshState(isRefreshing = true)
        viewModelScope.launch {
            var result = events.refreshEvent(eventId)
            // Opened before the event list was loaded: load it too.
            if (result is DataResult.Success && events.board(eventId).first() == null) {
                result = events.refreshEvents()
            }
            refreshState.value =
                RefreshState(error = (result as? DataResult.Failure)?.error)
        }
    }

    /** Tapping the selected item again clears the selection. */
    fun select(itemId: String) {
        selected.value = itemId.takeIf { it != selected.value }
    }

    /** Puts the selected waiting item into [slotId]. */
    fun assign(slotId: String) {
        val itemId = selected.value ?: return
        viewModelScope.launch {
            when (val result = intake.assign(slotId, itemId)) {
                is WriteResult.Saved -> selected.value = null
                is WriteResult.Refused -> message.value = result.reason
            }
        }
    }

    fun messageShown() {
        message.value = null
    }
}
