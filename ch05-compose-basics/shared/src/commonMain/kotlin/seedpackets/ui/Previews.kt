package seedpackets.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import seedpackets.model.SampleCatalog
import seedpackets.ui.theme.SeedTheme

@Preview(name = "Card, light", showBackground = true)
@Composable
private fun SeedPacketCardLightPreview() {
    SeedTheme(darkTheme = false) { CardSample() }
}

@Preview(name = "Card, dark", showBackground = true)
@Composable
private fun SeedPacketCardDarkPreview() {
    SeedTheme(darkTheme = true) { CardSample() }
}

@Composable
private fun CardSample() {
    Surface {
        SeedPacketCard(
            packet = SampleCatalog.packets.first(),
            isFavorite = true,
            onFavoriteChange = {},
            reserved = 2,
            onReservedChange = {},
        )
    }
}
