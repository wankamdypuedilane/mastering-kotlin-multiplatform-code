// ch05-compose-basics/shared/src/commonMain/.../ui/AdaptivePacketGrid.kt
package seedpackets.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import seedpackets.model.SeedPacket

/** One column below 600 dp; above, as many 280 dp columns as fit. */
fun columnsFor(width: Dp): Int =
    if (width < 600.dp) 1 else maxOf(2, (width / 280.dp).toInt())

/** Lays out [packets] for the space it is given, not for the device. */
@Composable
fun AdaptivePacketGrid(
    packets: List<SeedPacket>,
    modifier: Modifier = Modifier,
    card: @Composable (SeedPacket) -> Unit,
) {
    BoxWithConstraints(modifier) {         // maxWidth: the space we got
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsFor(maxWidth)),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(packets, key = { it.id }) { packet -> card(packet) }
        }
    }
}
