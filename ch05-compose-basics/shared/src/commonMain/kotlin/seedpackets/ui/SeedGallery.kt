package seedpackets.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import seedpackets.model.SeedPacket
import seedpackets.resources.Res
import seedpackets.resources.back_to_top
import seedpackets.resources.packets_reserved

/** Stateful: owns the gallery state and hoists it out of the content. */
@Composable
fun SeedGallery(packets: List<SeedPacket>, modifier: Modifier = Modifier) {
    var favorites by remember { mutableStateOf(setOf<String>()) }
    var reserved by remember { mutableStateOf(mapOf<String, Int>()) }

    SeedGalleryContent(
        packets = packets,
        favorites = favorites,
        reserved = reserved,
        onFavoriteChange = { id, on ->
            favorites = if (on) favorites + id else favorites - id
        },
        onReservedChange = { id, count -> reserved = reserved + (id to count) },
        modifier = modifier,
    )
}

/** Stateless content: easy to preview and to test. */
@Composable
fun SeedGalleryContent(
    packets: List<SeedPacket>,
    favorites: Set<String>,
    reserved: Map<String, Int>,
    onFavoriteChange: (id: String, on: Boolean) -> Unit,
    onReservedChange: (id: String, count: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val total = reserved.values.sum()
    val listState = rememberLazyListState()
    // Changes only when the answer flips, not on every scrolled pixel.
    val showBackToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    val scope = rememberCoroutineScope()

    Column(modifier) {
        Text(
            pluralStringResource(Res.plurals.packets_reserved, total, total),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp).testTag("total"),
        )
        Box(Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("gallery"),
            ) {
                items(packets, key = { it.id }) { packet ->
                    SeedPacketCard(
                        packet = packet,
                        isFavorite = packet.id in favorites,
                        onFavoriteChange = { onFavoriteChange(packet.id, it) },
                        reserved = reserved[packet.id] ?: 0,
                        onReservedChange = { onReservedChange(packet.id, it) },
                    )
                }
            }
            if (showBackToTop) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch { listState.animateScrollToItem(0) }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                ) {
                    Text(
                        stringResource(Res.string.back_to_top),
                        Modifier.padding(8.dp),
                    )
                }
            }
        }
    }
}
