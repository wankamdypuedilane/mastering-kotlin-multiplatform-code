package seedpackets.solutions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import seedpackets.model.Family
import seedpackets.model.SeedPacket
import seedpackets.model.inFamily
import seedpackets.resources.Res
import seedpackets.resources.all_families
import seedpackets.ui.SeedGallery

/** Exercise 5.2: stateless chips; `null` means "all families". */
@Composable
fun FamilyFilterRow(
    selected: Family?,
    onSelect: (Family?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(Res.string.all_families)) },
        )
        Family.entries.forEach { family ->
            FilterChip(
                selected = selected == family,
                onClick = { onSelect(family) },
                label = { Text(stringResource(family.label)) },
            )
        }
    }
}

/** The filter state lives here, the list is derived from it. */
@Composable
fun FilteredGallery(packets: List<SeedPacket>, modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf<Family?>(null) }
    val visible = remember(packets, selected) { packets.inFamily(selected) }
    Column(modifier) {
        FamilyFilterRow(selected, onSelect = { selected = it })
        SeedGallery(visible)
    }
}
