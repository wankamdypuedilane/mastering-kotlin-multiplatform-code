// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/App.kt
package hearthseed.shared

import androidx.compose.runtime.Composable
import hearthseed.shared.navigation.HearthseedNavHost
import hearthseed.shared.theme.HearthseedTheme

/** The whole app; Koin must be started first (initKoin). */
@Composable
fun App() {
    HearthseedTheme { HearthseedNavHost() }
}
