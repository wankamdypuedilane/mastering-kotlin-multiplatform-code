// mendfield/desktopApp/src/jvmMain/kotlin/mendfield/desktop/Main.kt
package mendfield.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import mendfield.shared.App
import mendfield.shared.di.AppConfig
import mendfield.shared.di.defaultBaseUrl
import mendfield.shared.di.initKoin

fun main() {
    // MENDFIELD_BASE_URL=http://192.168.1.20:8086/api/v1/ points elsewhere.
    val baseUrl = System.getenv("MENDFIELD_BASE_URL") ?: defaultBaseUrl()
    val koin = initKoin(AppConfig(baseUrl = baseUrl))
    application {
        Window(
            onCloseRequest = {
                koin.close() // closes Room and the HTTP client first
                exitApplication()
            },
            title = "Mendfield: Brindle Street Repair Café",
            state = rememberWindowState(width = 520.dp, height = 860.dp),
        ) { App() }
    }
}
