package seedswap.solutions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import seedswap.core.DraftCheck
import seedswap.core.Proposal
import seedswap.core.ProposalDraft
import seedswap.core.ProposalField
import seedswap.core.ProposalResult
import seedswap.core.SwapRepository
import seedswap.core.check
import seedswap.proposal.ProposalFormUiState

/** Exercise 6.3: the form in MVI style. Every input is an intent. */
sealed interface ProposalIntent {
    data class Edit(val field: ProposalField, val text: String) : ProposalIntent
    data object Submit : ProposalIntent
    data class SendFinished(val result: ProposalResult) : ProposalIntent
}

/** The next state, and the proposal to send if the reducer asks for it. */
data class Transition(
    val state: ProposalFormUiState,
    val send: Proposal? = null,
)

/** Pure reducer: (state, intent) -> new state. No coroutines, no ViewModel. */
fun reduce(
    state: ProposalFormUiState,
    intent: ProposalIntent,
    offerId: String,
): Transition = when (intent) {
    is ProposalIntent.Edit -> Transition(
        state.copy(
            draft = state.draft.with(intent.field, intent.text),
            errors = state.errors - intent.field,
        ),
    )
    ProposalIntent.Submit ->
        if (state.isSending || state.sentProposalId != null) Transition(state)
        else when (val check = state.draft.check(offerId)) {
            is DraftCheck.Invalid ->
                Transition(state.copy(errors = check.errors))
            is DraftCheck.Valid -> Transition(
                state.copy(isSending = true, errors = emptyMap()),
                send = check.proposal,
            )
        }
    is ProposalIntent.SendFinished -> Transition(
        when (val result = intent.result) {
            is ProposalResult.Sent -> state.copy(
                isSending = false,
                sentProposalId = result.proposalId,
            )
            ProposalResult.OfferNotFound ->
                state.copy(isSending = false, offerGone = true)
        },
    )
}

private fun ProposalDraft.with(field: ProposalField, text: String) =
    when (field) {
        ProposalField.MEMBER -> copy(member = text)
        ProposalField.VARIETY -> copy(variety = text)
        ProposalField.GRAMS -> copy(grams = text)
        ProposalField.MESSAGE -> copy(message = text)
    }

/** The ViewModel stores state and runs the effect the reducer asks for. */
class ProposalStore(
    private val offerId: String,
    private val repository: SwapRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProposalFormUiState())
    val uiState: StateFlow<ProposalFormUiState> = _uiState.asStateFlow()

    fun dispatch(intent: ProposalIntent) {
        val next = reduce(_uiState.value, intent, offerId)
        _uiState.value = next.state
        next.send?.let { proposal ->
            viewModelScope.launch {
                val result = repository.propose(proposal)
                dispatch(ProposalIntent.SendFinished(result))
            }
        }
    }
}
