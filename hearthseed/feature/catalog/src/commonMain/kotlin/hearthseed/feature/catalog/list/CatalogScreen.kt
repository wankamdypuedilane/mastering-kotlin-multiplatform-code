// hearthseed/feature/catalog/src/commonMain/kotlin/hearthseed/feature/catalog/list/CatalogScreen.kt
package hearthseed.feature.catalog.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hearthseed.core.data.DataError
import hearthseed.core.data.userMessage
import hearthseed.core.model.Variety
import hearthseed.feature.catalog.FamilyBadge
import hearthseed.feature.catalog.label

/** Stateful entry: collects the ViewModel's state and forwards events. */
@Composable
fun CatalogRoute(
    viewModel: CatalogViewModel,
    onVarietyClick: (varietyId: String) -> Unit,
    onOpenLoans: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CatalogScreen(
        state = state,
        actions = CatalogActions(
            onQueryChange = viewModel::onQueryChange,
            onFamilySelected = viewModel::onFamilySelected,
            onVarietyClick = onVarietyClick,
            onLoadMore = viewModel::loadMore,
            onRefresh = viewModel::refresh,
            onRetry = viewModel::retry,
            onOpenLoans = onOpenLoans,
        ),
    )
}

/** Every event of the screen: the stateless screen takes two parameters. */
class CatalogActions(
    val onQueryChange: (String) -> Unit = {},
    val onFamilySelected: (String?) -> Unit = {},
    val onVarietyClick: (String) -> Unit = {},
    val onLoadMore: () -> Unit = {},
    val onRefresh: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onOpenLoans: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(state: CatalogUiState, actions: CatalogActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Larkmoor seed library") },
                actions = {
                    // Pull-to-refresh needs touch; mouse users get a button.
                    TextButton(onClick = actions.onRefresh) { Text("Refresh") }
                    TextButton(onClick = actions.onOpenLoans) {
                        Text("My loans")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = actions.onQueryChange,
                label = { Text("Search varieties") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            FamilyFilter(state.families, state.family, actions.onFamilySelected)
            state.refreshError?.let {
                Text(
                    "Not refreshed: ${it.userMessage()}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = actions.onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                ListArea(state.list, actions)
            }
        }
    }
}

@Composable
private fun FamilyFilter(
    families: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(selected == null, onClick = { onSelect(null) },
                label = { Text("All") })
        }
        items(families) { family ->
            FilterChip(
                selected = family == selected,
                // Tapping the selected family again clears the filter.
                onClick = { onSelect(family.takeIf { it != selected }) },
                label = { Text(family) },
            )
        }
    }
}

@Composable
private fun ListArea(list: ListState, actions: CatalogActions) {
    when (list) {
        ListState.Loading -> Centered {
            CircularProgressIndicator(Modifier.testTag("loading"))
        }
        is ListState.Error -> Centered {
            Text(list.error.userMessage())
            Button(onClick = actions.onRetry) { Text("Retry") }
        }
        ListState.Empty -> Centered { Text("No varieties match your search.") }
        is ListState.Items -> LazyColumn(
            Modifier.fillMaxSize().testTag("varieties"),
        ) {
            items(list.varieties, key = { it.id }) { variety ->
                VarietyRow(variety) { actions.onVarietyClick(variety.id) }
                HorizontalDivider()
            }
            item(key = "footer") { ListFooter(list, actions.onLoadMore) }
        }
    }
}

@Composable
private fun ListFooter(list: ListState.Items, onLoadMore: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
        when {
            list.isLoadingMore -> CircularProgressIndicator()
            list.loadMoreError != null ->
                LoadMoreError(list.loadMoreError, onLoadMore)
            list.hasMore -> Button(onClick = onLoadMore) {
                Text("Load more (${list.varieties.size} of ${list.total})")
            }
            else -> Text("All shown (${list.total})")
        }
    }
}

@Composable
private fun LoadMoreError(error: DataError, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(error.userMessage(), color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun VarietyRow(variety: Variety, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FamilyBadge(variety.family)
        Column {
            Text(variety.commonName, style = MaterialTheme.typography.titleMedium)
            Text(variety.latinName, fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodySmall)
            Text(
                "${variety.family} · ${variety.daysToMaturity} days · " +
                    variety.difficulty.label(),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}
