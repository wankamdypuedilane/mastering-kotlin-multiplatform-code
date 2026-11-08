// ch22-release/webApp/src/wasmJsMain/kotlin/release/Main.kt
package release

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport { AboutScreen() }
}
