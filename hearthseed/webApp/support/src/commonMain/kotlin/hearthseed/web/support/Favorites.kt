// hearthseed/webApp/support/src/commonMain/kotlin/hearthseed/web/support/Favorites.kt
package hearthseed.web.support

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** A starred variety; the name lets the panel show it without the network. */
@Serializable
data class Favorite(val varietyId: String, val name: String)

/** The member's favorite varieties, newest last. */
interface FavoritesStore {
    val favorites: StateFlow<List<Favorite>>
    fun toggle(favorite: Favorite)
}

/** Keeps the list as JSON under one key of a [KeyValueStorage]. */
class StorageFavoritesStore(
    private val storage: KeyValueStorage,
    private val key: String = KEY,
) : FavoritesStore {
    private val state =
        MutableStateFlow(FavoritesCodec.decode(storage.read(key)))
    override val favorites: StateFlow<List<Favorite>> = state.asStateFlow()

    override fun toggle(favorite: Favorite) {
        val next = state.updateAndGet { list ->
            if (list.any { it.varietyId == favorite.varietyId }) {
                list.filterNot { it.varietyId == favorite.varietyId }
            } else {
                list + favorite
            }
        }
        storage.write(key, FavoritesCodec.encode(next))
    }

    companion object {
        /** Versioned: a new format gets a new key, old data is ignored. */
        const val KEY = "hearthseed.favorites.v1"
    }
}

object FavoritesCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(favorites: List<Favorite>): String =
        json.encodeToString(favorites)

    /** Missing or damaged data must not break the app: start empty. */
    fun decode(text: String?): List<Favorite> {
        if (text.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<Favorite>>(text)
                .distinctBy { it.varietyId }
        } catch (e: SerializationException) {
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }
}
