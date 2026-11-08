package seedswap.board.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import seedswap.core.SwapOffer
import seedswap.core.SwapRepository

sealed interface OfferDetailUiState {
    data object Loading : OfferDetailUiState
    data object NotFound : OfferDetailUiState
    data class Content(val offer: SwapOffer) : OfferDetailUiState
}

class OfferDetailViewModel(
    offerId: String,
    repository: SwapRepository,
) : ViewModel() {
    val uiState: StateFlow<OfferDetailUiState> = repository.offer(offerId)
        .map { offer ->
            if (offer == null) OfferDetailUiState.NotFound
            else OfferDetailUiState.Content(offer)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            OfferDetailUiState.Loading,
        )
}
