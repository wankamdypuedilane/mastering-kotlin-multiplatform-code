// hearthseed/androidApp/src/main/kotlin/hearthseed/android/MainActivity.kt
package hearthseed.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import hearthseed.shared.App
import hearthseed.shared.platform.LocalLoanReminders
import hearthseed.shared.platform.LocalSharer

/** The only activity: everything else is a destination of the NavHost. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw behind the system bars; Scaffold and TopAppBar apply insets.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            // Activity-scoped platform actions for the shared UI.
            val sharer = remember { AndroidSharer(this) }
            CompositionLocalProvider(
                LocalSharer provides sharer,
                LocalLoanReminders provides rememberLoanReminders(),
            ) {
                App()
            }
        }
    }
}
