// hearthseed/desktopApp/src/jvmMain/kotlin/hearthseed/desktop/Main.kt
package hearthseed.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import hearthseed.shared.App
import hearthseed.shared.di.AppConfig
import hearthseed.shared.di.defaultBaseUrl
import hearthseed.shared.di.initKoin

fun main() {
    // HEARTHSEED_BASE_URL=http://192.168.1.20:8085/api/v1/ points elsewhere.
    val baseUrl = System.getenv("HEARTHSEED_BASE_URL") ?: defaultBaseUrl()
    initKoin(AppConfig(baseUrl = baseUrl))
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Hearthseed",
            state = rememberWindowState(width = 480.dp, height = 800.dp),
        ) { App() }
    }
}
