// hearthseed/feature/catalog/src/commonMain/kotlin/hearthseed/feature/catalog/detail/VarietyDetailViewModel.kt
package hearthseed.feature.catalog.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hearthseed.core.data.CatalogRepository
import hearthseed.core.data.DataError
import hearthseed.core.data.DataResult
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VarietyDetailUiState(
    val variety: Variety? = null,
    val varietyError: DataError? = null,
    val lots: List<SeedLot>? = null, // null until the first load
    val lotsError: DataError? = null,
)

class VarietyDetailViewModel(
    private val varietyId: String,
    private val repository: CatalogRepository,
) : ViewModel() {
    private val loaded = MutableStateFlow(VarietyDetailUiState())

    // Lots come from the repository's local copy: a borrow made on another
    // screen updates this one without a reload.
    val state: StateFlow<VarietyDetailUiState> =
        combine(loaded, repository.observeLots(varietyId)) { s, lots ->
            s.copy(lots = lots)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
            VarietyDetailUiState())

    init { load() }

    fun retry() = load()

    private fun load() {
        loaded.update { it.copy(varietyError = null, lotsError = null) }
        viewModelScope.launch {
            val result = repository.variety(varietyId)
            loaded.update {
                when (result) {
                    is DataResult.Success -> it.copy(variety = result.value)
                    is DataResult.Failure ->
                        it.copy(varietyError = result.error)
                }
            }
        }
        viewModelScope.launch { // in parallel with the variety
            val result = repository.refreshLots(varietyId)
            if (result is DataResult.Failure) {
                loaded.update { it.copy(lotsError = result.error) }
            }
        }
    }
}
