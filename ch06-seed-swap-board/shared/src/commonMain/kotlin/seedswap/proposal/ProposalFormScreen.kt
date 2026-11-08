package seedswap.proposal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import seedswap.core.FieldError
import seedswap.core.ProposalField
import seedswap.core.ProposalRules
import seedswap.core.SwapRepository

@Composable
fun ProposalFormRoute(
    offerId: String,
    repository: SwapRepository,
    onSent: (proposalId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ProposalFormViewModel =
        viewModel { ProposalFormViewModel(offerId, repository) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // The ViewModel reports "sent" as state; the UI reacts by navigating away.
    LaunchedEffect(state.sentProposalId) {
        state.sentProposalId?.let(onSent)
    }
    ProposalFormScreen(
        state = state,
        onMemberChange = viewModel::onMemberChange,
        onVarietyChange = viewModel::onVarietyChange,
        onGramsChange = viewModel::onGramsChange,
        onMessageChange = viewModel::onMessageChange,
        onSubmit = viewModel::onSubmit,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposalFormScreen(
    state: ProposalFormUiState,
    onMemberChange: (String) -> Unit,
    onVarietyChange: (String) -> Unit,
    onGramsChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Propose a swap") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val draft = state.draft
            FormField("Your name", draft.member, state.errors[ProposalField.MEMBER], onMemberChange)
            FormField("Variety you give", draft.variety, state.errors[ProposalField.VARIETY], onVarietyChange)
            FormField("Grams", draft.grams, state.errors[ProposalField.GRAMS], onGramsChange)
            FormField("Message (optional)", draft.message, state.errors[ProposalField.MESSAGE], onMessageChange)
            if (state.offerGone) {
                Text("This offer was withdrawn.", color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = onSubmit, enabled = !state.isSending) {
                Text(if (state.isSending) "Sending…" else "Send proposal")
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    error: FieldError?,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it.message()) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** UI text for a domain error: the domain stays free of strings. */
fun FieldError.message(): String = when (this) {
    FieldError.REQUIRED -> "Required"
    FieldError.NOT_A_NUMBER -> "Enter a whole number of grams"
    FieldError.OUT_OF_RANGE ->
        "Between ${ProposalRules.MIN_GRAMS} and ${ProposalRules.MAX_GRAMS} g"
    FieldError.TOO_LONG -> "At most ${ProposalRules.MAX_MESSAGE} characters"
}
