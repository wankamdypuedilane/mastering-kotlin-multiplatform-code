// hearthseed/webApp/support/src/commonMain/kotlin/hearthseed/web/support/solutions/Ch18Solutions.kt
package hearthseed.web.support.solutions

import hearthseed.web.support.Favorite
import hearthseed.web.support.FavoritesCodec
import hearthseed.web.support.FavoritesStore
import hearthseed.web.support.KeyValueStorage
import hearthseed.web.support.StorageFavoritesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Exercise 18.1: the browser tab shows how many favorites there are.
fun tabTitle(favoriteCount: Int): String = when (favoriteCount) {
    0 -> "Hearthseed"
    1 -> "Hearthseed (1 favorite)"
    else -> "Hearthseed ($favoriteCount favorites)"
}

// Exercise 18.2: "?session" in the address keeps favorites for this tab only.
enum class StorageChoice { LOCAL, SESSION }

/** [search] is location.search: "", "?session" or "?a=1&session=yes". */
fun storageChoice(search: String): StorageChoice {
    val names = search.removePrefix("?").split('&')
        .map { it.substringBefore('=') }
    return if ("session" in names) StorageChoice.SESSION
    else StorageChoice.LOCAL
}

// Exercise 18.3: another tab changed localStorage; read it again.
class SyncedFavoritesStore(
    private val storage: KeyValueStorage,
    private val key: String = StorageFavoritesStore.KEY,
) : FavoritesStore {
    private var store = StorageFavoritesStore(storage, key)
    private val state = MutableStateFlow(store.favorites.value)
    override val favorites: StateFlow<List<Favorite>> = state.asStateFlow()

    override fun toggle(favorite: Favorite) {
        store.toggle(favorite)
        state.value = store.favorites.value
    }

    /** From the storage event; [changedKey] is null after clear(). */
    fun onStorageChanged(changedKey: String?) {
        if (changedKey != null && changedKey != key) return
        store = StorageFavoritesStore(storage, key) // decodes the new value
        state.value = store.favorites.value
    }
}

// Exercise 18.4: the last varieties opened in this tab, newest first.
class RecentlyViewed(
    private val storage: KeyValueStorage,
    private val capacity: Int = 5,
    private val key: String = KEY,
) {
    private val state = MutableStateFlow(
        FavoritesCodec.decode(storage.read(key)).take(capacity))
    val items: StateFlow<List<Favorite>> = state.asStateFlow()

    fun record(variety: Favorite) {
        val next = (listOf(variety) +
            state.value.filterNot { it.varietyId == variety.varietyId })
            .take(capacity)
        if (next == state.value) return // reopening the newest: no write
        state.value = next
        storage.write(key, FavoritesCodec.encode(next))
    }

    companion object { const val KEY = "hearthseed.recent.v1" }
}
