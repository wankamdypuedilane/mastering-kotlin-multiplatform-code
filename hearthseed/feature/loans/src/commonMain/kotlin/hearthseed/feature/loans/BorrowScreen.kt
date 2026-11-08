// hearthseed/feature/loans/src/commonMain/kotlin/hearthseed/feature/loans/BorrowScreen.kt
package hearthseed.feature.loans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hearthseed.core.data.userMessage

@Composable
fun BorrowRoute(
    viewModel: BorrowViewModel,
    lotSummary: String,
    onShowLoans: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BorrowScreen(state, lotSummary, viewModel::borrow, onShowLoans, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowScreen(
    state: BorrowUiState,
    lotSummary: String,
    onBorrow: () -> Unit,
    onShowLoans: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Borrow seed") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(lotSummary, style = MaterialTheme.typography.titleMedium)
            when (state) {
                BorrowUiState.Confirm -> {
                    Text("Grow it this season and return fresh seed after the harvest.")
                    Button(onClick = onBorrow) { Text("Borrow this lot") }
                }
                BorrowUiState.Sending -> CircularProgressIndicator()
                is BorrowUiState.Borrowed -> {
                    Text("Borrowed on ${state.loan.borrowedOn}. " +
                        "Return seed after the ${state.loan.dueSeason} season.")
                    Button(onClick = onShowLoans) { Text("See my loans") }
                }
                is BorrowUiState.Failed -> {
                    Text(state.error.userMessage(), color = MaterialTheme.colorScheme.error)
                    if (state.canRetry) {
                        Button(onClick = onBorrow) { Text("Try again") }
                    }
                    OutlinedButton(onClick = onBack) { Text("Back to the variety") }
                }
            }
        }
    }
}
