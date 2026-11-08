// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/bridge/VarietyDetailObserver.kt
package hearthseed.shared.bridge

import androidx.lifecycle.viewModelScope
import hearthseed.core.data.userMessage
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety
import hearthseed.feature.catalog.detail.VarietyDetailUiState
import hearthseed.feature.catalog.detail.VarietyDetailViewModel
import kotlinx.coroutines.flow.map

/** One variety and its lots, as Swift reads them. */
data class VarietyDetailSnapshot(
    val variety: Variety?, // null while loading or after an error
    val lots: List<SeedLot>,
    val isLoading: Boolean,
    val message: String?,
)

internal fun VarietyDetailUiState.toSnapshot() = VarietyDetailSnapshot(
    variety = variety,
    lots = lots.orEmpty(),
    isLoading = (variety == null && varietyError == null) ||
        (lots == null && lotsError == null),
    message = (varietyError ?: lotsError)?.userMessage(),
)

/** The detail screen for SwiftUI; one observer per variety. */
class VarietyDetailObserver internal constructor(
    create: () -> VarietyDetailViewModel,
) {
    private val holder = ViewModelHolder(VarietyDetailViewModel::class, create)
    private val viewModel get() = holder.viewModel

    fun observe(onChange: (VarietyDetailSnapshot) -> Unit): FlowSubscription =
        viewModel.viewModelScope.subscribe(
            viewModel.state.map { it.toSnapshot() }, onChange,
        )

    fun retry() = viewModel.retry()
    fun close() = holder.clear()
}
