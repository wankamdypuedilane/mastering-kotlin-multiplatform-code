// hearthseed/feature/loans/src/commonMain/kotlin/hearthseed/feature/loans/MyLoansViewModel.kt
package hearthseed.feature.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hearthseed.core.data.DataError
import hearthseed.core.data.DataResult
import hearthseed.core.data.LoanRepository
import hearthseed.core.model.Loan
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MyLoansUiState {
    data object Loading : MyLoansUiState
    data class Error(val error: DataError) : MyLoansUiState
    data object Empty : MyLoansUiState
    data class Loans(val open: List<Loan>, val returned: List<Loan>) : MyLoansUiState
}

class MyLoansViewModel(private val repository: LoanRepository) : ViewModel() {
    private val _state = MutableStateFlow<MyLoansUiState>(MyLoansUiState.Loading)
    val state: StateFlow<MyLoansUiState> = _state.asStateFlow()
    private var job: Job? = null

    init { refresh() }

    fun refresh() {
        if (job?.isActive == true) return
        _state.value = MyLoansUiState.Loading
        job = viewModelScope.launch {
            _state.value = when (val result = repository.myLoans()) {
                is DataResult.Failure -> MyLoansUiState.Error(result.error)
                is DataResult.Success ->
                    if (result.value.isEmpty()) MyLoansUiState.Empty
                    else result.value.partition { it.isOpen }
                        .let { (open, returned) -> MyLoansUiState.Loans(open, returned) }
            }
        }
    }
}
