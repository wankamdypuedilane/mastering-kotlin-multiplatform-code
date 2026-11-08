package seedswap.proposal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import seedswap.core.DraftCheck
import seedswap.core.FieldError
import seedswap.core.ProposalDraft
import seedswap.core.ProposalField
import seedswap.core.ProposalResult
import seedswap.core.SwapRepository
import seedswap.core.check

data class ProposalFormUiState(
    val draft: ProposalDraft = ProposalDraft(),
    val errors: Map<ProposalField, FieldError> = emptyMap(),
    val isSending: Boolean = false,
    val offerGone: Boolean = false,
    val sentProposalId: String? = null,  // the "sent" event, kept as state
)

class ProposalFormViewModel(
    private val offerId: String,
    private val repository: SwapRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProposalFormUiState())
    val uiState: StateFlow<ProposalFormUiState> = _uiState.asStateFlow()

    fun onMemberChange(text: String) =
        edit(ProposalField.MEMBER) { copy(member = text) }
    fun onVarietyChange(text: String) =
        edit(ProposalField.VARIETY) { copy(variety = text) }
    fun onGramsChange(text: String) =
        edit(ProposalField.GRAMS) { copy(grams = text) }
    fun onMessageChange(text: String) =
        edit(ProposalField.MESSAGE) { copy(message = text) }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isSending || current.sentProposalId != null) return
        when (val check = current.draft.check(offerId)) {
            is DraftCheck.Invalid ->
                _uiState.update { it.copy(errors = check.errors) }
            is DraftCheck.Valid -> {
                _uiState.update {
                    it.copy(isSending = true, errors = emptyMap())
                }
                viewModelScope.launch {
                    val result = repository.propose(check.proposal)
                    _uiState.update {
                        when (result) {
                            is ProposalResult.Sent -> it.copy(
                                isSending = false,
                                sentProposalId = result.proposalId,
                            )
                            ProposalResult.OfferNotFound ->
                                it.copy(isSending = false, offerGone = true)
                        }
                    }
                }
            }
        }
    }

    /** Typing in a field clears that field's error until the next submit. */
    private fun edit(
        field: ProposalField,
        change: ProposalDraft.() -> ProposalDraft,
    ) =
        _uiState.update {
            it.copy(draft = it.draft.change(), errors = it.errors - field)
        }
}
