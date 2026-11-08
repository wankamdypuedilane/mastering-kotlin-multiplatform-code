package labels.app

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import labels.core.LabelSize
import labels.core.MAX_SOWING_TEXT

@Composable
fun LabelEditor(state: EditorState, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxSize()) {
        Row {
            VarietyList(state, Modifier.width(260.dp).fillMaxHeight())
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SeedLabelPreview(state.design, Modifier.testTag("preview"))
                OutlinedTextField(
                    value = state.sowingText,
                    onValueChange = { state.sowingText = it },
                    label = { Text("Sowing text") },
                    isError = state.design.problem != null,
                    supportingText = {
                        Text(state.design.problem
                            ?: "${state.sowingText.length}/$MAX_SOWING_TEXT")
                    },
                    modifier = Modifier.width(420.dp).testTag("sowing-text"),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabelSize.entries.forEach { size ->
                        FilterChip(
                            selected = state.size == size,
                            onClick = { state.size = size },
                            label = { Text(size.name.lowercase()) },
                        )
                    }
                }
                Text(state.status, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/** Click to select; Up and Down arrows move the selection when focused. */
@Composable
private fun VarietyList(state: EditorState, modifier: Modifier) {
    LazyColumn(
        modifier
            .testTag("variety-list")
            .onPreviewKeyEvent { event ->
                val down = event.type == KeyEventType.KeyDown
                when {
                    down && event.key == Key.DirectionDown -> {
                        state.selectNext(+1); true
                    }
                    down && event.key == Key.DirectionUp -> {
                        state.selectNext(-1); true
                    }
                    else -> false // let other keys reach the list
                }
            }
            .focusable(),
    ) {
        items(state.varieties, key = { it.id }) { variety ->
            val selected = variety == state.selected
            ListItem(
                headlineContent = { Text(variety.commonName) },
                supportingContent = { Text(variety.latinName) },
                colors = ListItemDefaults.colors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier
                    .selectable(selected, onClick = { state.select(variety) })
                    .testTag("variety-${variety.id}"),
            )
        }
    }
}
