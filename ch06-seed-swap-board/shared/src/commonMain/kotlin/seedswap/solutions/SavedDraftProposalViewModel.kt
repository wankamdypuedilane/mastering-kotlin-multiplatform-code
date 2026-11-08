package seedswap.solutions

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import seedswap.core.DraftCheck
import seedswap.core.ProposalDraft
import seedswap.core.ProposalField
import seedswap.core.ProposalResult
import seedswap.core.SwapRepository
import seedswap.core.check
import seedswap.proposal.ProposalFormUiState

/**
 * Exercise 6.2: the form ViewModel, with its draft kept in a SavedStateHandle.
 * Screens obtain it with savedDraftProposalViewModel() below.
 */
class SavedDraftProposalViewModel(
    private val offerId: String,
    private val handle: SavedStateHandle,
    private val repository: SwapRepository,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(ProposalFormUiState(draft = restore()))
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

    private fun edit(
        field: ProposalField,
        change: ProposalDraft.() -> ProposalDraft,
    ) {
        _uiState.update {
            it.copy(draft = it.draft.change(), errors = it.errors - field)
        }
        save(_uiState.value.draft)
    }

    // Plain strings are saveable on every platform.
    private fun restore() = ProposalDraft(
        member = handle[KEY_MEMBER] ?: "",
        variety = handle[KEY_VARIETY] ?: "",
        grams = handle[KEY_GRAMS] ?: "",
        message = handle[KEY_MESSAGE] ?: "",
    )

    private fun save(draft: ProposalDraft) {
        handle[KEY_MEMBER] = draft.member
        handle[KEY_VARIETY] = draft.variety
        handle[KEY_GRAMS] = draft.grams
        handle[KEY_MESSAGE] = draft.message
    }

    private companion object {
        const val KEY_MEMBER = "draft.member"
        const val KEY_VARIETY = "draft.variety"
        const val KEY_GRAMS = "draft.grams"
        const val KEY_MESSAGE = "draft.message"
    }
}

/** How a screen obtains it: the initializer receives the CreationExtras. */
@Composable
fun savedDraftProposalViewModel(
    offerId: String,
    repository: SwapRepository,
): SavedDraftProposalViewModel = viewModel {
    SavedDraftProposalViewModel(offerId, createSavedStateHandle(), repository)
}
