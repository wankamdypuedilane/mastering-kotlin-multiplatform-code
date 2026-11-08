// hearthseed/feature/catalog/src/commonMain/kotlin/hearthseed/feature/catalog/detail/VarietyDetailScreen.kt
package hearthseed.feature.catalog.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hearthseed.core.data.userMessage
import hearthseed.core.model.LotStatus
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety
import hearthseed.feature.catalog.FamilyBadge
import hearthseed.feature.catalog.label
import hearthseed.feature.catalog.monthsLabel
import hearthseed.feature.catalog.summary

@Composable
fun VarietyDetailRoute(
    viewModel: VarietyDetailViewModel,
    onBorrow: (lot: SeedLot, variety: Variety) -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    VarietyDetailScreen(state, onBorrow, onBack, onRetry = viewModel::retry)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VarietyDetailScreen(
    state: VarietyDetailUiState,
    onBorrow: (SeedLot, Variety) -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.variety?.commonName ?: "Variety") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        val variety = state.variety
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.varietyError != null -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.varietyError.userMessage())
                    Button(onClick = onRetry) { Text("Retry") }
                }
                variety == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { VarietyHeader(variety) }
                    item { Text("Seed lots", style = MaterialTheme.typography.titleLarge) }
                    lotsSection(state, onRetry) { lot -> onBorrow(lot, variety) }
                }
            }
        }
    }
}

@Composable
private fun VarietyHeader(variety: Variety) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            FamilyBadge(variety.family, size = 64.dp)
            Column {
                Text(variety.latinName, fontStyle = FontStyle.Italic)
                Text(variety.family)
            }
        }
        Text(variety.description)
        Text("Sow in ${monthsLabel(variety.sowingMonths)}; " +
            "ready in ${variety.daysToMaturity} days; ${variety.difficulty.label()}.")
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.lotsSection(
    state: VarietyDetailUiState,
    onRetry: () -> Unit,
    onBorrow: (SeedLot) -> Unit,
) {
    val lots = state.lots
    when {
        lots == null && state.lotsError != null -> item {
            Text(state.lotsError.userMessage())
            TextButton(onClick = onRetry) { Text("Retry") }
        }
        lots == null -> item { CircularProgressIndicator() }
        lots.isEmpty() -> item { Text("No lots of this variety yet.") }
        else -> items(lots, key = { it.id }) { lot -> LotRow(lot, onBorrow) }
    }
}

@Composable
private fun LotRow(lot: SeedLot, onBorrow: (SeedLot) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(lot.summary())
            Text(
                when (lot.status) {
                    LotStatus.AVAILABLE -> "Available"
                    LotStatus.ON_LOAN -> "On loan"
                    LotStatus.RETIRED -> "Retired"
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (lot.isBorrowable) Button(onClick = { onBorrow(lot) }) { Text("Borrow") }
    }
}
