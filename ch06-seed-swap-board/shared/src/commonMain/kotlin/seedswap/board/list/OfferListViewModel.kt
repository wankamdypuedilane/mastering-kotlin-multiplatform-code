package seedswap.board.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import seedswap.core.SwapOffer
import seedswap.core.SwapRepository

/** Everything the list screen can show, and nothing else. */
sealed interface OfferListUiState {
    data object Loading : OfferListUiState
    data object Empty : OfferListUiState
    data class Content(val offers: List<SwapOffer>) : OfferListUiState
}

class OfferListViewModel(repository: SwapRepository) : ViewModel() {
    val uiState: StateFlow<OfferListUiState> = repository.offers()
        .map { offers ->
            if (offers.isEmpty()) OfferListUiState.Empty
            else OfferListUiState.Content(offers)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OfferListUiState.Loading,
        )
}
