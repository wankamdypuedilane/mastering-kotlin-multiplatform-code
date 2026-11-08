// hearthseed/feature/catalog/src/commonMain/kotlin/hearthseed/feature/catalog/list/CatalogViewModel.kt
package hearthseed.feature.catalog.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hearthseed.core.data.CatalogRepository
import hearthseed.core.data.DataError
import hearthseed.core.data.DataResult
import hearthseed.core.data.VarietyPage
import hearthseed.core.model.BotanicalFamilies
import hearthseed.core.model.Variety
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What the list area shows: exactly one of four situations. */
sealed interface ListState {
    data object Loading : ListState
    data class Error(val error: DataError) : ListState
    data object Empty : ListState
    data class Items(
        val varieties: List<Variety>,
        val total: Int,
        val loadedPage: Int,
        val hasMore: Boolean,
        val isLoadingMore: Boolean = false,
        val loadMoreError: DataError? = null,
    ) : ListState
}

data class CatalogUiState(
    val query: String = "",
    val family: String? = null, // null = every family
    val families: List<String> = BotanicalFamilies.all,
    val list: ListState = ListState.Loading,
    val isRefreshing: Boolean = false,
    val refreshError: DataError? = null, // the old items stay on screen
)

class CatalogViewModel(
    private val repository: CatalogRepository,
    private val searchDelay: Duration = 300.milliseconds,
) : ViewModel() {
    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    // At most one request at a time; a new filter cancels the old one.
    private var loadJob: Job? = null

    init { reload() }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        reload(wait = searchDelay) // each keystroke restarts the wait
    }

    fun onFamilySelected(family: String?) {
        _state.update { it.copy(family = family) }
        reload()
    }

    fun retry() = reload()

    fun refresh() {
        if (_state.value.list !is ListState.Items) return reload()
        loadJob?.cancel() // a refresh replaces a "load more" in flight
        _state.update { it.copy(isRefreshing = true, refreshError = null) }
        updateItems { it.copy(isLoadingMore = false) }
        loadJob = viewModelScope.launch {
            val result = firstPage()
            _state.update {
                when (result) {
                    is DataResult.Success -> it.copy(
                        list = result.value.toListState(), isRefreshing = false,
                    )
                    is DataResult.Failure -> it.copy(
                        isRefreshing = false, refreshError = result.error,
                    )
                }
            }
        }
    }

    fun loadMore() {
        val items = _state.value.list as? ListState.Items ?: return
        if (!items.hasMore || loadJob?.isActive == true) return
        updateItems { it.copy(isLoadingMore = true, loadMoreError = null) }
        loadJob = viewModelScope.launch {
            val s = _state.value
            val next = items.loadedPage + 1
            when (val result = repository.varieties(s.query, s.family, next)) {
                is DataResult.Success -> updateItems { it.append(result.value) }
                is DataResult.Failure -> updateItems {
                    it.copy(isLoadingMore = false, loadMoreError = result.error)
                }
            }
        }
    }

    private fun reload(wait: Duration = Duration.ZERO) {
        loadJob?.cancel()
        _state.update {
            it.copy(list = ListState.Loading, isRefreshing = false,
                refreshError = null)
        }
        loadJob = viewModelScope.launch {
            delay(wait) // cancelled by the next keystroke: a debounce
            val result = firstPage()
            _state.update {
                it.copy(list = when (result) {
                    is DataResult.Success -> result.value.toListState()
                    is DataResult.Failure -> ListState.Error(result.error)
                })
            }
        }
    }

    // Reads the filters when the request starts, after the debounce.
    private suspend fun firstPage(): DataResult<VarietyPage> =
        _state.value.let { repository.varieties(it.query, it.family, page = 1) }

    private fun updateItems(change: (ListState.Items) -> ListState.Items) =
        _state.update { s ->
            val items = s.list as? ListState.Items ?: return@update s
            s.copy(list = change(items))
        }
}

private fun VarietyPage.toListState(): ListState =
    if (items.isEmpty() && !hasMore) ListState.Empty
    else ListState.Items(items, total, page, hasMore)

private fun ListState.Items.append(next: VarietyPage) = copy(
    // distinctBy: a variety added on the server can shift pages by one.
    varieties = (varieties + next.items).distinctBy { it.id },
    total = next.total,
    loadedPage = next.page,
    hasMore = next.hasMore,
    isLoadingMore = false,
)
