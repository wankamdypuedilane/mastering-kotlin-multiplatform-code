// hearthseed/webApp/src/wasmJsMain/kotlin/hearthseed/web/solutions/Ch18Wiring.kt
package hearthseed.web.solutions

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import hearthseed.web.support.Favorite
import hearthseed.web.support.FavoritesStore
import hearthseed.web.support.solutions.RecentlyViewed
import hearthseed.web.support.solutions.SyncedFavoritesStore
import hearthseed.web.support.solutions.sessionStorageOrMemory
import hearthseed.web.support.solutions.storageForPage
import hearthseed.web.support.solutions.tabTitle
import kotlinx.browser.document
import kotlinx.browser.window
import org.koin.dsl.module
import org.w3c.dom.StorageEvent
import org.w3c.dom.events.Event

// Exercise 18.1: call in WebShell with favorites.size.
@Composable
fun TabTitleEffect(favoriteCount: Int) {
    LaunchedEffect(favoriteCount) { document.title = tabTitle(favoriteCount) }
}

// Exercises 18.2 and 18.3: replaces webModule in main().
val solutionWebModule = module {
    single { SyncedFavoritesStore(storageForPage()) }
    single<FavoritesStore> { get<SyncedFavoritesStore>() }
    single { RecentlyViewed(sessionStorageOrMemory()) }
}

// Exercise 18.3: call once in WebShell; removed when it leaves composition.
@Composable
fun StorageSyncEffect(store: SyncedFavoritesStore) {
    DisposableEffect(store) {
        val listener: (Event) -> Unit = { event ->
            store.onStorageChanged((event as StorageEvent).key)
        }
        window.addEventListener("storage", listener)
        onDispose { window.removeEventListener("storage", listener) }
    }
}

// Exercise 18.4: record the shown variety; list the recent ones in the panel.
@Composable
fun RecentlyViewedSection(
    recent: RecentlyViewed,
    shown: Favorite?,
    onOpen: (varietyId: String) -> Unit,
) {
    LaunchedEffect(shown) { shown?.let(recent::record) }
    val items by recent.items.collectAsState()
    if (items.isEmpty()) return
    Text("Recently viewed", style = MaterialTheme.typography.titleSmall)
    items.forEach { item ->
        TextButton(onClick = { onOpen(item.varietyId) }) { Text(item.name) }
    }
}
