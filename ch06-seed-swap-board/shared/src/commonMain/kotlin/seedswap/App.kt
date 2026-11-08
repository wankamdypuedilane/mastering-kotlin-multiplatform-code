package seedswap

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import seedswap.core.SwapRepository
import seedswap.navigation.SeedSwapNavHost

@Composable
fun App(repository: SwapRepository = AppContainer.repository) {
    MaterialTheme {
        SeedSwapNavHost(repository)
    }
}
