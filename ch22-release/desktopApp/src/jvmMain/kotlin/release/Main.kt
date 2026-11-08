// ch22-release/desktopApp/src/jvmMain/kotlin/release/Main.kt
package release

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Hearthseed") { AboutScreen() }
}
