package seedswap.board.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import seedswap.core.SwapRepository

@Composable
fun OfferDetailRoute(
    offerId: String,
    repository: SwapRepository,
    onPropose: () -> Unit,
    onBack: () -> Unit,
    viewModel: OfferDetailViewModel =
        viewModel { OfferDetailViewModel(offerId, repository) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OfferDetailScreen(state, onPropose, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(state: OfferDetailUiState, onPropose: () -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offer") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (state) {
                OfferDetailUiState.Loading -> CircularProgressIndicator()
                OfferDetailUiState.NotFound -> Text("This offer is no longer on the board.")
                is OfferDetailUiState.Content -> {
                    val offer = state.offer
                    Text(offer.variety, style = MaterialTheme.typography.headlineSmall)
                    Text("Family: ${offer.family}")
                    Text("${offer.grams} g offered by ${offer.member}")
                    Text("Hopes to receive: ${offer.wanted}")
                    Text("Proposals so far: ${offer.proposals}")
                    Button(onClick = onPropose) { Text("Propose a swap") }
                }
            }
        }
    }
}
