package hearthseed.labels.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LabelScreen(viewModel: LabelViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MaterialTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Printing from ${state.host}")
            Button(onClick = viewModel::printAll) { Text("Print all labels") }
            state.labels.forEach { Text(it) }
            LibraryFooter()
        }
    }
}

/** A plain dependency (not a ViewModel) injected straight into a composable. */
@Composable
private fun LibraryFooter(host: PrinterHost = koinInject()) {
    Text(
        "Labels are formatted on ${host.name}",
        style = MaterialTheme.typography.bodySmall,
    )
}
