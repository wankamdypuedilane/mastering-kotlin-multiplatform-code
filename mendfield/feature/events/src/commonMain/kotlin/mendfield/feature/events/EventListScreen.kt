// mendfield/feature/events/src/commonMain/kotlin/mendfield/feature/events/EventListScreen.kt
package mendfield.feature.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mendfield.core.ui.ErrorBanner
import mendfield.core.ui.FullScreenError
import mendfield.core.ui.FullScreenLoading

@Composable
fun EventListRoute(viewModel: EventListViewModel, onOpenEvent: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventListScreen(state, onOpenEvent, onRefresh = viewModel::refresh)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    state: EventListUiState,
    onOpenEvent: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Brindle Street Repair Café") },
                actions = { TextButton(onClick = onRefresh) { Text("Refresh") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            val error = state.refreshError
            when {
                !state.cacheRead -> FullScreenLoading()
                state.events.isEmpty() && state.isRefreshing -> FullScreenLoading()
                state.events.isEmpty() && error != null ->
                    FullScreenError(error, onRefresh)
                state.events.isEmpty() -> Text("No events yet.", Modifier.padding(16.dp))
                else -> {
                    if (error != null) ErrorBanner(error, onRefresh)
                    LazyColumn(Modifier.fillMaxSize().testTag("events")) {
                        // A stable key: rows keep their state when the list moves.
                        items(state.events, key = { it.id }) { row ->
                            EventListRow(row) { onOpenEvent(row.id) }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventListRow(row: EventRow, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp)) {
        Text(row.title, style = MaterialTheme.typography.titleMedium)
        Text(row.whenText, style = MaterialTheme.typography.bodyMedium)
        Text(row.venue, style = MaterialTheme.typography.bodySmall)
    }
}
