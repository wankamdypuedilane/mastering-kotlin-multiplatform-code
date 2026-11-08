// mendfield/feature/events/src/commonMain/kotlin/mendfield/feature/events/EventListViewModel.kt
package mendfield.feature.events

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
import mendfield.core.data.DataError
import mendfield.core.data.DataResult
import mendfield.core.data.EventRepository
import mendfield.core.model.Event
import mendfield.core.ui.hhmm
import mendfield.core.ui.label

/** One line of the list, already formatted: the screen only lays it out. */
@Immutable
data class EventRow(
    val id: String,
    val title: String,
    val whenText: String, // "Sat 21 Nov 2026, 10:00–13:00"
    val venue: String,
)

/**
 * @Immutable promises Compose that an instance never changes, so equal
 * states skip recomposition even though List is not a stable type.
 */
@Immutable
data class EventListUiState(
    val cacheRead: Boolean = false, // false until the database answered
    val events: List<EventRow> = emptyList(),
    val isRefreshing: Boolean = false,
    val refreshError: DataError? = null,
)

/** Refresh progress, kept apart from the data it refreshes. */
internal data class RefreshState(
    val isRefreshing: Boolean = false,
    val error: DataError? = null,
)

class EventListViewModel(private val repository: EventRepository) : ViewModel() {
    private val refreshState = MutableStateFlow(RefreshState())

    val state: StateFlow<EventListUiState> =
        combine(repository.events, refreshState) { events, refresh ->
            EventListUiState(
                cacheRead = true,
                events = events.map { it.toRow() },
                isRefreshing = refresh.isRefreshing,
                refreshError = refresh.error,
            )
        }.stateIn(
            viewModelScope,
            // Stop reading the database 5 s after the screen goes away (but
            // not during a rotation), start again when it comes back.
            SharingStarted.WhileSubscribed(5_000),
            EventListUiState(),
        )

    init { refresh() }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        refreshState.value = RefreshState(isRefreshing = true)
        viewModelScope.launch {
            val result = repository.refreshEvents()
            refreshState.update {
                RefreshState(error = (result as? DataResult.Failure)?.error)
            }
        }
    }
}

internal fun Event.toRow() = EventRow(
    id = id,
    title = title,
    whenText = "${date.label()}, ${opensAt.hhmm()}–${closesAt.hhmm()}",
    venue = venue,
)
