// hearthseed/feature/loans/src/commonMain/kotlin/hearthseed/feature/loans/MyLoansScreen.kt
package hearthseed.feature.loans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hearthseed.core.data.userMessage
import hearthseed.core.model.Loan

@Composable
fun MyLoansRoute(viewModel: MyLoansViewModel, memberName: String, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MyLoansScreen(state, memberName, viewModel::refresh, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLoansScreen(
    state: MyLoansUiState,
    memberName: String,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loans of $memberName") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = { TextButton(onClick = onRefresh) { Text("Refresh") } },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
            when (state) {
                MyLoansUiState.Loading -> CircularProgressIndicator()
                is MyLoansUiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.userMessage())
                    Button(onClick = onRefresh) { Text("Retry") }
                }
                MyLoansUiState.Empty -> Text("No loans yet. Borrow a lot from the catalog.")
                is MyLoansUiState.Loans -> LazyColumn(
                    Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Text("Growing now", style = MaterialTheme.typography.titleLarge) }
                    items(state.open, key = { it.id }) { LoanRow(it) }
                    if (state.returned.isNotEmpty()) {
                        item { Text("Returned", style = MaterialTheme.typography.titleLarge) }
                        items(state.returned, key = { it.id }) { LoanRow(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanRow(loan: Loan) {
    Column {
        Text("Lot ${loan.lotId}", style = MaterialTheme.typography.titleMedium)
        Text(
            if (loan.isOpen) "Borrowed ${loan.borrowedOn}, due after the ${loan.dueSeason} season"
            else "Returned ${loan.returnedGrams} g from the ${loan.dueSeason} harvest",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
