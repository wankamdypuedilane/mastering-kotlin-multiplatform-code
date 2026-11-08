// mendfield/feature/events/src/commonMain/kotlin/mendfield/feature/events/EventBoardScreen.kt
package mendfield.feature.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mendfield.core.ui.ErrorBanner
import mendfield.core.ui.FullScreenError
import mendfield.core.ui.FullScreenLoading

/** The navigation of the board: the app shell decides where each one goes. */
class BoardNavigation(
    val onBack: () -> Unit = {},
    val onRegisterItem: () -> Unit = {},
    val onOpenItem: (itemId: String) -> Unit = {},
    val onOpenStats: () -> Unit = {},
)

@Composable
fun EventBoardRoute(viewModel: EventBoardViewModel, navigation: BoardNavigation) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventBoardScreen(
        state = state,
        navigation = navigation,
        actions = BoardActions(
            onRefresh = viewModel::refresh,
            onSelect = viewModel::select,
            onAssign = viewModel::assign,
            onMessageShown = viewModel::messageShown,
        ),
    )
}

class BoardActions(
    val onRefresh: () -> Unit = {},
    val onSelect: (itemId: String) -> Unit = {},
    val onAssign: (slotId: String) -> Unit = {},
    val onMessageShown: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBoardScreen(
    state: BoardUiState,
    navigation: BoardNavigation,
    actions: BoardActions,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            actions.onMessageShown()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.title)
                        Text(state.subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = { TextButton(onClick = navigation.onBack) { Text("Back") } },
                actions = {
                    TextButton(onClick = navigation.onOpenStats) { Text("Stats") }
                    TextButton(onClick = actions.onRefresh) { Text("Refresh") }
                },
            )
        },
        floatingActionButton = {
            if (state.status is BoardStatus.Ready) {
                ExtendedFloatingActionButton(onClick = navigation.onRegisterItem) {
                    Text("Register item")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (val status = state.status) {
                BoardStatus.Loading -> FullScreenLoading()
                is BoardStatus.Missing -> status.error?.let {
                    FullScreenError(it, actions.onRefresh)
                } ?: Text("This event is not on this device.", Modifier.padding(16.dp))
                is BoardStatus.Ready -> {
                    state.refreshError?.let { ErrorBanner(it, actions.onRefresh) }
                    BoardList(status.content, navigation, actions)
                }
            }
        }
    }
}

@Composable
private fun BoardList(
    content: BoardContent,
    navigation: BoardNavigation,
    actions: BoardActions,
) {
    LazyColumn(Modifier.fillMaxSize().testTag("board")) {
        item(key = "counts") { CountsRow(content.counts) }
        waitingSection(content, actions)
        content.schedule.forEach { fixer ->
            item(key = "fixer-${fixer.fixerId}") {
                Column(Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                    Text(fixer.name, style = MaterialTheme.typography.titleMedium)
                    Text(fixer.skills, style = MaterialTheme.typography.bodySmall)
                }
            }
            // Keys are slot ids: an assignment updates one row in place.
            items(fixer.slots, key = { it.slotId }) { slot ->
                SlotLine(slot, assigning = content.selection != null,
                    onOpenItem = navigation.onOpenItem, onAssign = actions.onAssign)
            }
        }
        item(key = "bottom-space") { Spacer(Modifier.height(88.dp)) } // FAB room
    }
}

@Composable
private fun CountsRow(counts: BoardCounts) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Count("Registered", counts.registered)
        Count("Waiting", counts.waiting)
        Count("In a slot", counts.scheduled)
        Count("Done", counts.withOutcome)
    }
}

@Composable
private fun Count(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

private fun LazyListScope.waitingSection(content: BoardContent, actions: BoardActions) {
    item(key = "waiting-title") {
        Text("Waiting for a slot", Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium)
    }
    if (content.waiting.isEmpty()) {
        item(key = "waiting-none") {
            Text("Every item has a slot.", Modifier.padding(16.dp))
        }
    }
    items(content.waiting, key = { "waiting-${it.itemId}" }) { row ->
        Surface(
            tonalElevation = if (row.isSelected) 4.dp else 0.dp,
            modifier = Modifier.fillMaxWidth().clickable { actions.onSelect(row.itemId) },
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(row.title, fontWeight = if (row.isSelected) FontWeight.Bold else null)
                Text(row.visitor, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    content.selection?.let { selection ->
        item(key = "suggestions") { Suggestions(selection, actions.onAssign) }
    }
}

@Composable
private fun Suggestions(selection: Selection, onAssign: (String) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp).testTag("suggestions")) {
        if (selection.suggestions.isEmpty()) {
            Text("No fixer with a free, suitable slot. Tap an open slot below.")
        } else {
            Text("Suggested by triage:", style = MaterialTheme.typography.labelLarge)
        }
        selection.suggestions.forEach { suggestion ->
            AssistChip(onClick = { onAssign(suggestion.slotId) },
                label = { Text(suggestion.text) })
        }
    }
}

@Composable
private fun SlotLine(
    slot: SlotRow,
    assigning: Boolean,
    onOpenItem: (String) -> Unit,
    onAssign: (String) -> Unit,
) {
    val click: (() -> Unit)? = when {
        slot.itemId != null -> { { onOpenItem(slot.itemId) } }
        assigning -> { { onAssign(slot.slotId) } }
        else -> null
    }
    Row(
        Modifier.fillMaxWidth()
            .let { if (click != null) it.clickable(onClick = click) else it }
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("slot-${slot.slotId}"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(slot.time, style = MaterialTheme.typography.bodyMedium)
        Text(
            slot.itemText ?: if (assigning) "Open: tap to assign" else "Open",
            Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        slot.outcome?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
    }
}
