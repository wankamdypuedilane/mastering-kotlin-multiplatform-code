package seedswap.board.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import seedswap.core.SwapOffer
import seedswap.core.SwapRepository

/** Stateful entry: owns the ViewModel and collects its state. */
@Composable
fun OfferListRoute(
    repository: SwapRepository,
    onOfferClick: (offerId: String) -> Unit,
    viewModel: OfferListViewModel =
        viewModel { OfferListViewModel(repository) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OfferListScreen(state, onOfferClick)
}

/** Stateless screen: state in, events out. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferListScreen(state: OfferListUiState, onOfferClick: (String) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Seed Swap Board") }) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
            when (state) {
                OfferListUiState.Loading -> CircularProgressIndicator()
                OfferListUiState.Empty -> Text("No offers yet.")
                is OfferListUiState.Content -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.offers, key = { it.id }) { offer ->
                        OfferRow(offer, onClick = { onOfferClick(offer.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferRow(offer: SwapOffer, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp)) {
        Text(offer.variety, style = MaterialTheme.typography.titleMedium)
        Text(
            "${offer.grams} g from ${offer.member}, wants: ${offer.wanted}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
