// hearthseed/feature/loans/src/commonMain/kotlin/hearthseed/feature/loans/BorrowViewModel.kt
package hearthseed.feature.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hearthseed.core.data.DataError
import hearthseed.core.data.DataResult
import hearthseed.core.data.LoanRepository
import hearthseed.core.model.Loan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BorrowUiState {
    data object Confirm : BorrowUiState
    data object Sending : BorrowUiState
    data class Borrowed(val loan: Loan) : BorrowUiState
    data class Failed(val error: DataError) : BorrowUiState {
        /** 409: trying again cannot help; another lot might. */
        val canRetry: Boolean get() = error != DataError.LotUnavailable
    }
}

class BorrowViewModel(
    private val lotId: String,
    private val repository: LoanRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<BorrowUiState>(BorrowUiState.Confirm)
    val state: StateFlow<BorrowUiState> = _state.asStateFlow()

    fun borrow() {
        val current = _state.value
        // One POST at a time; never a second loan after a success.
        if (current is BorrowUiState.Sending) return
        if (current is BorrowUiState.Borrowed) return
        _state.value = BorrowUiState.Sending
        viewModelScope.launch {
            _state.value = when (val result = repository.borrow(lotId)) {
                is DataResult.Success -> BorrowUiState.Borrowed(result.value)
                is DataResult.Failure -> BorrowUiState.Failed(result.error)
            }
        }
    }
}
