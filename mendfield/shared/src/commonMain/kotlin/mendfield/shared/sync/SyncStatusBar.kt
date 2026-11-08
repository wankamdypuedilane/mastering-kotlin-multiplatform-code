// mendfield/shared/src/commonMain/kotlin/mendfield/shared/sync/SyncStatusBar.kt
package mendfield.shared.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SyncStatusBar(viewModel: SyncStatusViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SyncStatusBar(state, viewModel::syncNow, viewModel::workOffline, viewModel::dismiss)
}

@Composable
fun SyncStatusBar(
    state: SyncBarUiState,
    onSyncNow: () -> Unit,
    onWorkOffline: (Boolean) -> Unit,
    onDismiss: (String) -> Unit,
) {
    var reviewing by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Surface(
        color = when (state.tone) {
            Tone.CALM -> colors.surfaceVariant
            Tone.BUSY -> colors.secondaryContainer
            Tone.WARNING -> colors.tertiaryContainer
        },
        modifier = Modifier.fillMaxWidth().testTag("sync-bar"),
    ) {
        Row(
            Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(state.text, Modifier.weight(1f).testTag("sync-text"),
                style = MaterialTheme.typography.bodySmall)
            if (state.refused.isNotEmpty()) {
                TextButton(onClick = { reviewing = true }) {
                    Text("Refused (${state.refused.size})")
                }
            }
            if (state.pending > 0 && !state.workingOffline) {
                TextButton(onClick = onSyncNow) { Text("Sync now") }
            }
            Text("Offline", style = MaterialTheme.typography.labelSmall)
            Switch(checked = state.workingOffline, onCheckedChange = onWorkOffline,
                modifier = Modifier.testTag("work-offline"))
        }
    }
    if (reviewing && state.refused.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { reviewing = false },
            title = { Text("Refused by the server") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("The board shows the server's version again.")
                    state.refused.forEach { row ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(row.text, Modifier.weight(1f))
                            TextButton(onClick = { onDismiss(row.operationId) }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { reviewing = false }) { Text("Close") } },
        )
    }
}
