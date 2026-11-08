// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/bridge/CatalogObserver.kt
package hearthseed.shared.bridge

import androidx.lifecycle.viewModelScope
import hearthseed.core.data.userMessage
import hearthseed.core.model.Variety
import hearthseed.feature.catalog.list.CatalogUiState
import hearthseed.feature.catalog.list.CatalogViewModel
import hearthseed.feature.catalog.list.ListState
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Which of the four list situations the screen shows. */
enum class CatalogPhase { LOADING, FAILED, EMPTY, LOADED }

/** The catalog screen as Swift reads it: plain values, no sealed types. */
data class CatalogSnapshot(
    val phase: CatalogPhase,
    val varieties: List<Variety>,
    val total: Int,
    val message: String?, // a failed load, refresh or next page; or null
    val canLoadMore: Boolean,
    val isLoadingMore: Boolean,
    val query: String,
    val family: String?,
    val families: List<String>,
)

internal fun CatalogUiState.toSnapshot(): CatalogSnapshot {
    val items = list as? ListState.Items
    val phase = when (list) {
        ListState.Loading -> CatalogPhase.LOADING
        is ListState.Error -> CatalogPhase.FAILED
        ListState.Empty -> CatalogPhase.EMPTY
        is ListState.Items -> CatalogPhase.LOADED
    }
    val error = (list as? ListState.Error)?.error
        ?: refreshError ?: items?.loadMoreError
    return CatalogSnapshot(
        phase = phase,
        varieties = items?.varieties.orEmpty(),
        total = items?.total ?: 0,
        message = error?.userMessage(),
        canLoadMore = items?.hasMore == true && !items.isLoadingMore,
        isLoadingMore = items?.isLoadingMore == true,
        query = query, family = family, families = families,
    )
}

/** The catalog list for SwiftUI, over the same ViewModel as Compose. */
class CatalogObserver internal constructor(create: () -> CatalogViewModel) {
    private val holder = ViewModelHolder(CatalogViewModel::class, create)
    internal val viewModel get() = holder.viewModel

    /** Calls [onChange] now and after every change, on the main thread. */
    fun observe(onChange: (CatalogSnapshot) -> Unit): FlowSubscription =
        viewModel.viewModelScope.subscribe(
            viewModel.state.map { it.toSnapshot() }, onChange,
        )

    fun search(query: String) = viewModel.onQueryChange(query)

    /** Swift sees `throws`: a family that is not in the list is a bug. */
    @Throws(IllegalArgumentException::class)
    fun selectFamily(family: String?) {
        require(family == null || family in viewModel.state.value.families) {
            "Unknown family: $family"
        }
        viewModel.onFamilySelected(family)
    }

    fun loadMore() = viewModel.loadMore()
    fun retry() = viewModel.retry()

    /** Swift: `try await observer.refresh()`; returns when it is done. */
    @Throws(CancellationException::class)
    suspend fun refresh() {
        viewModel.viewModelScope.async { // cancelled by close()
            viewModel.refresh()
            viewModel.state.first {
                !it.isRefreshing && it.list != ListState.Loading
            }
        }.await()
    }

    /** Clears the ViewModel: its work and every subscription stop. */
    fun close() = holder.clear()
}
