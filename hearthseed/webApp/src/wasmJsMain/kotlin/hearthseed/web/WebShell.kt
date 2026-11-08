// hearthseed/webApp/src/wasmJsMain/kotlin/hearthseed/web/WebShell.kt
package hearthseed.web

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import hearthseed.core.data.CatalogRepository
import hearthseed.core.data.DataResult
import hearthseed.shared.navigation.HearthseedNavHost
import hearthseed.shared.navigation.VarietyDetail
import hearthseed.shared.theme.HearthseedTheme
import hearthseed.web.support.Favorite
import hearthseed.web.support.FavoritesStore
import org.koin.compose.koinInject

/** The shared navigation, plus a favorites panel that only the web has. */
@Composable
fun WebShell(
    store: FavoritesStore = koinInject(),
    catalog: CatalogRepository = koinInject(),
) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val shownId = entry?.takeIf { it.destination.hasRoute<VarietyDetail>() }
        ?.toRoute<VarietyDetail>()?.varietyId
    var shown by remember { mutableStateOf<Favorite?>(null) }
    LaunchedEffect(shownId) { // the variety on screen, for the star button
        val result = shownId?.let { catalog.variety(it) }
        shown = (result as? DataResult.Success)?.value
            ?.let { Favorite(it.id, it.commonName) }
    }
    val favorites by store.favorites.collectAsState()

    HearthseedTheme {
        Surface {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val wide = maxWidth >= 720.dp // a phone browser gets no list
                val panel = if (wide) Modifier.width(240.dp) else Modifier
                Row(Modifier.fillMaxSize()) {
                    FavoritesPanel(
                        favorites, shown, showList = wide,
                        onToggle = store::toggle,
                        onOpen = { navController.navigate(VarietyDetail(it)) },
                        modifier = panel,
                    )
                    Box(Modifier.weight(1f)) {
                        HearthseedNavHost(navController) // all shared screens
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesPanel(
    favorites: List<Favorite>,
    shown: Favorite?,
    showList: Boolean,
    onToggle: (Favorite) -> Unit,
    onOpen: (varietyId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxHeight().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (shown != null) {
            val starred = favorites.any { it.varietyId == shown.varietyId }
            FilledTonalButton(onClick = { onToggle(shown) }) {
                Text(if (starred) "★ Starred" else "☆ Star")
            }
        }
        if (!showList) return@Column
        Text("Favorites", style = MaterialTheme.typography.titleMedium)
        if (favorites.isEmpty()) {
            Text("Open a variety and star it.",
                style = MaterialTheme.typography.bodySmall)
        }
        favorites.forEach { favorite ->
            TextButton(onClick = { onOpen(favorite.varietyId) },
                modifier = Modifier.fillMaxWidth()) { Text(favorite.name) }
        }
    }
}
