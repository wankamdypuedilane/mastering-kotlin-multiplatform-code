package hearthseed.labels.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import hearthseed.labels.ui.LabelScreen
import hearthseed.labels.ui.appModules
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

fun main() = application {
    // Koin starts with the composition and is available to everything inside.
    KoinApplication(
        configuration = koinConfiguration { modules(appModules()) },
    ) {
        Window(onCloseRequest = ::exitApplication, title = "Packet Labels") {
            LabelScreen()
        }
    }
}
