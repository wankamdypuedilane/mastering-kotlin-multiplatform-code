// mendfield/shared/src/commonMain/kotlin/mendfield/shared/App.kt
package mendfield.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mendfield.core.ui.MendfieldTheme
import mendfield.shared.navigation.MendfieldNavHost
import mendfield.shared.sync.SyncStatusBar
import org.koin.compose.viewmodel.koinViewModel

/** The whole organizer app; Koin must be started first (initKoin). */
@Composable
fun App() {
    MendfieldTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                // The bar below pads for the navigation bar: the screens
                // above must not pad for it a second time.
                MendfieldNavHost(Modifier.weight(1f)
                    .consumeWindowInsets(WindowInsets.navigationBars))
                SyncStatusBar(koinViewModel()) // on every screen
            }
        }
    }
}
