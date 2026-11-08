package seedswap.solutions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import seedswap.board.list.OfferListUiState
import seedswap.core.SwapRepository

/** Exercise 6.1: the list with a search field. */
class SearchableOfferListViewModel(repository: SwapRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow() // for the TextField

    val uiState: StateFlow<OfferListUiState> =
        combine(repository.offers(), _query) { offers, query ->
            val hits = offers.filter {
                it.variety.contains(query.trim(), ignoreCase = true)
            }
            if (hits.isEmpty()) OfferListUiState.Empty
            else OfferListUiState.Content(hits)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            OfferListUiState.Loading,
        )

    fun onQueryChange(text: String) {
        _query.value = text
    }
}
