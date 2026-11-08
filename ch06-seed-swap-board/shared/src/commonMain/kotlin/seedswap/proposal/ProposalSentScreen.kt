package seedswap.proposal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProposalSentScreen(proposalId: String, onViewOffer: () -> Unit, onBackToBoard: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Proposal sent", style = MaterialTheme.typography.headlineSmall)
        Text("Reference: $proposalId")
        Button(onClick = onBackToBoard) { Text("Back to the board") }
        OutlinedButton(onClick = onViewOffer) { Text("View the offer") }
    }
}
