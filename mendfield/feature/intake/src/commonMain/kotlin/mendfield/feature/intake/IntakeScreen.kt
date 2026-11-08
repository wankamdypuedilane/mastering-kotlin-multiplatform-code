// mendfield/feature/intake/src/commonMain/kotlin/mendfield/feature/intake/IntakeScreen.kt
package mendfield.feature.intake

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mendfield.core.data.DraftProblem
import mendfield.core.data.ItemLimits
import mendfield.core.model.ItemCategory
import mendfield.core.ui.label

@Composable
fun IntakeRoute(
    viewModel: IntakeViewModel,
    onRegistered: (itemId: String) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // A one-off navigation, driven by state: it survives a rotation.
    LaunchedEffect(state.savedItemId) { state.savedItemId?.let(onRegistered) }
    IntakeScreen(state, IntakeActions(
        onCategory = viewModel::onCategory,
        onDescription = viewModel::onDescription,
        onVisitorName = viewModel::onVisitorName,
        onSave = viewModel::save,
        onBack = onBack,
    ))
}

class IntakeActions(
    val onCategory: (ItemCategory) -> Unit = {},
    val onDescription: (String) -> Unit = {},
    val onVisitorName: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onBack: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IntakeScreen(state: IntakeUiState, actions: IntakeActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register an item") },
                navigationIcon = { TextButton(onClick = actions.onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(state.eventTitle, style = MaterialTheme.typography.titleMedium)
            Text("Category", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItemCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.form.category == category,
                        onClick = { actions.onCategory(category) },
                        label = { Text(category.label()) },
                    )
                }
            }
            OutlinedTextField(
                value = state.form.description,
                onValueChange = actions.onDescription,
                label = { Text("What is broken?") },
                isError = state.problems.any { it.isDescription },
                supportingText = { Text(descriptionHelp(state.problems)) },
                modifier = Modifier.fillMaxWidth().testTag("description"),
            )
            OutlinedTextField(
                value = state.form.visitorName,
                onValueChange = actions.onVisitorName,
                label = { Text("Visitor's name") },
                singleLine = true,
                isError = state.problems.any { !it.isDescription },
                supportingText = { Text(visitorHelp(state.problems)) },
                modifier = Modifier.fillMaxWidth().testTag("visitor"),
            )
            if (state.hints.isNotEmpty()) {
                Text("Triage suggests", style = MaterialTheme.typography.labelLarge)
                state.hints.forEach { Text("• ${it.text}") }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = actions.onSave, enabled = state.canSave) {
                Text(if (state.isSaving) "Saving…" else "Save item")
            }
        }
    }
}

private val DraftProblem.isDescription: Boolean
    get() = this == DraftProblem.DESCRIPTION_EMPTY ||
        this == DraftProblem.DESCRIPTION_TOO_LONG

private fun descriptionHelp(problems: Set<DraftProblem>) = when {
    DraftProblem.DESCRIPTION_EMPTY in problems -> "Describe the problem."
    DraftProblem.DESCRIPTION_TOO_LONG in problems ->
        "At most ${ItemLimits.DESCRIPTION} characters."
    else -> "A few words for the fixer."
}

private fun visitorHelp(problems: Set<DraftProblem>) = when {
    DraftProblem.VISITOR_EMPTY in problems -> "Who brought it?"
    DraftProblem.VISITOR_TOO_LONG in problems ->
        "At most ${ItemLimits.VISITOR_NAME} characters."
    else -> "So the fixer can call them."
}
