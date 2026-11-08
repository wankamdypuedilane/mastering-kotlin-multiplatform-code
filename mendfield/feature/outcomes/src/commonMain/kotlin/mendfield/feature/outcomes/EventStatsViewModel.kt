// mendfield/feature/outcomes/src/commonMain/kotlin/mendfield/feature/outcomes/EventStatsViewModel.kt
package mendfield.feature.outcomes

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import mendfield.core.data.EventRepository
import mendfield.core.data.OutcomeRepository
import mendfield.core.domain.OutcomeStats
import mendfield.core.ui.label

@Immutable
data class StatLine(val label: String, val value: String)

@Immutable
data class StatsUiState(
    val title: String = "",
    val isLoading: Boolean = true,
    val headline: String = "",      // "78 % repaired"
    val lines: List<StatLine> = emptyList(),
    val byCategory: List<StatLine> = emptyList(),
)

/**
 * The statistics of :core:domain on the device copy: they include outcomes
 * logged offline, before the server has seen them.
 */
class EventStatsViewModel(
    eventId: String,
    events: EventRepository,
    outcomes: OutcomeRepository,
) : ViewModel() {
    val state: StateFlow<StatsUiState> =
        combine(events.board(eventId), outcomes.stats(eventId)) { board, stats ->
            stats.toUiState(board?.event?.title.orEmpty())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())
}

internal fun OutcomeStats.toUiState(title: String) = StatsUiState(
    title = title,
    isLoading = false,
    headline = repairRatePercent?.let { "$it % repaired" } ?: "No repairs tried yet",
    lines = listOf(
        StatLine("Registered", "$registered"),
        StatLine("With an outcome", "$withOutcome"),
        StatLine("Still waiting", "$pending"),
    ) + counts.map { (kind, count) -> StatLine(kind.label(), "$count") },
    byCategory = repairRateByCategory.map { (category, rate) ->
        StatLine(category.label(), "$rate %")
    },
)
