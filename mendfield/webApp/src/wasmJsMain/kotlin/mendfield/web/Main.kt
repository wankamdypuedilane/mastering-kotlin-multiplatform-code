// mendfield/webApp/src/wasmJsMain/kotlin/mendfield/web/Main.kt
package mendfield.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import mendfield.shared.App
import mendfield.shared.di.initKoin

/**
 * The time-zone database for kotlinx-datetime: without it TimeZone.of(
 * "Europe/London") throws on Wasm, and every event of the server has it.
 */
@JsModule("@js-joda/timezone")
external object JsJodaTimeZoneModule

private val jsJodaTz = JsJodaTimeZoneModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    jsJodaTz // touch it: the module loads before the first TimeZone.of
    initKoin() // the page's origin, an in-memory cache, a localStorage outbox
    // Compose draws into a canvas inside <div id="mendfield"> (index.html).
    ComposeViewport(viewportContainerId = "mendfield") { App() }
}
