package seedpackets.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import seedpackets.model.SeedPacket
import seedpackets.resources.Res
import seedpackets.resources.days_to_harvest
import seedpackets.resources.favorite_add
import seedpackets.resources.favorite_remove
import seedpackets.resources.heart_filled
import seedpackets.resources.heart_outline
import seedpackets.resources.packets_left
import seedpackets.resources.seed_packet
import seedpackets.resources.sold_out

/** One packet. Stateless: every value comes in, every change goes out. */
@Composable
fun SeedPacketCard(
    packet: SeedPacket,
    isFavorite: Boolean,
    onFavoriteChange: (Boolean) -> Unit,
    reserved: Int,
    onReservedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth().testTag("card-${packet.id}")) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PacketBadge()
            Column(
                Modifier.weight(1f).padding(horizontal = 12.dp)
                    .semantics(mergeDescendants = true) {},  // read as one
            ) {
                Text(packet.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(
                        Res.string.days_to_harvest, packet.daysToMaturity,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StockLine(packet.packetsInStock)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FavoriteToggle(packet.name, isFavorite, onFavoriteChange)
                if (packet.isAvailable) {
                    QuantityStepper(
                        value = reserved,
                        onValueChange = onReservedChange,
                        range = 0..packet.packetsInStock,
                    )
                }
            }
        }
    }
}

@Composable
private fun PacketBadge() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        Image(
            painter = painterResource(Res.drawable.seed_packet),
            contentDescription = null,              // decorative
            colorFilter = ColorFilter.tint(
                MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
private fun StockLine(stock: Int) {
    if (stock == 0) {
        Text(
            stringResource(Res.string.sold_out),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error,
        )
    } else {
        Text(
            pluralStringResource(Res.plurals.packets_left, stock, stock),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun FavoriteToggle(
    name: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    IconToggleButton(checked = checked, onCheckedChange = onChange) {
        Icon(
            painter = painterResource(
                if (checked) Res.drawable.heart_filled
                else Res.drawable.heart_outline,
            ),
            contentDescription = stringResource(
                if (checked) Res.string.favorite_remove
                else Res.string.favorite_add,
                name,
            ),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
