// hearthseed/webApp/src/wasmJsMain/kotlin/hearthseed/web/Main.kt
package hearthseed.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import hearthseed.shared.di.AppConfig
import hearthseed.shared.di.initKoin
import hearthseed.web.support.FavoritesStore
import hearthseed.web.support.StorageFavoritesStore
import hearthseed.web.support.apiBaseUrl
import hearthseed.web.support.localStorageOrMemory
import kotlinx.browser.window
import org.koin.dsl.module

/** What only the web app adds to the shared Koin graph. */
val webModule = module {
    single<FavoritesStore> { StorageFavoritesStore(localStorageOrMemory()) }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val baseUrl = apiBaseUrl(pageConfig()?.apiBaseUrl, window.location.origin)
    initKoin(AppConfig(baseUrl = baseUrl)) { modules(webModule) }
    // Compose draws into a canvas inside <div id="hearthseed"> (index.html).
    ComposeViewport(viewportContainerId = "hearthseed") { WebShell() }
}
