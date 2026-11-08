// mendfield/feature/outcomes/src/commonMain/kotlin/mendfield/feature/outcomes/OutcomeScreens.kt
package mendfield.feature.outcomes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import mendfield.core.model.OutcomeKind
import mendfield.core.ui.FullScreenLoading
import mendfield.core.ui.label

@Composable
fun LogOutcomeRoute(viewModel: LogOutcomeViewModel, onDone: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onDone() }
    LogOutcomeScreen(state, viewModel::onKind, viewModel::onNote, viewModel::save, onDone)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogOutcomeScreen(
    state: LogOutcomeUiState,
    onKind: (OutcomeKind) -> Unit,
    onNote: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log an outcome") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        val item = state.item
        if (item !is ItemStatus.Ready) {
            Column(Modifier.padding(padding)) {
                if (item is ItemStatus.Missing) {
                    Text("This item is not on this device.", Modifier.padding(16.dp))
                } else FullScreenLoading()
            }
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(item.summary.title, style = MaterialTheme.typography.titleMedium)
            Text("${item.summary.visitor} · ${item.summary.slot}")
            item.summary.history.forEach {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutcomeKind.entries.forEach { kind ->
                    FilterChip(state.kind == kind, onClick = { onKind(kind) },
                        label = { Text(kind.label()) })
                }
            }
            OutlinedTextField(
                value = state.note,
                onValueChange = onNote,
                label = { Text("Note for the visitor (optional)") },
                isError = state.noteTooLong,
                modifier = Modifier.fillMaxWidth().testTag("note"),
            )
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onSave, enabled = state.canSave) { Text("Save outcome") }
        }
    }
}

@Composable
fun EventStatsRoute(viewModel: EventStatsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventStatsScreen(state, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventStatsScreen(state: StatsUiState, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title.ifEmpty { "Statistics" }) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Column(Modifier.padding(padding)) { FullScreenLoading() }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("stats")) {
            item(key = "headline") {
                Text(state.headline, Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall)
            }
            items(state.lines, key = { "line-${it.label}" }) { StatRow(it) }
            if (state.byCategory.isNotEmpty()) {
                item(key = "by-category") {
                    Text("Repair rate by category", Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium)
                }
                items(state.byCategory, key = { "cat-${it.label}" }) { StatRow(it) }
            }
        }
    }
}

@Composable
private fun StatRow(line: StatLine) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(line.label, Modifier.weight(1f))
        Text(line.value)
    }
}
