// hearthseed/webApp/support/src/commonTest/kotlin/hearthseed/web/support/FavoritesTest.kt
package hearthseed.web.support

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoritesTest {
    private val beans = Favorite("v-01", "Larkmoor Runner Bean")
    private val chard = Favorite("v-07", "Rainbow Chard")

    @Test
    fun toggleAddsThenRemoves() {
        val store = StorageFavoritesStore(InMemoryStorage())
        store.toggle(beans)
        store.toggle(chard)
        assertEquals(listOf(beans, chard), store.favorites.value)
        store.toggle(beans)
        assertEquals(listOf(chard), store.favorites.value)
    }

    @Test
    fun aNewStoreReadsWhatTheLastOneWrote() { // a page reload
        val storage = InMemoryStorage()
        StorageFavoritesStore(storage).toggle(chard)
        val reloaded = StorageFavoritesStore(storage)
        assertEquals(listOf(chard), reloaded.favorites.value)
    }

    @Test
    fun damagedDataStartsEmpty() {
        val damaged = listOf(null, "", "not json", "{\"varietyId\":1}", "[1,2]")
        for (text in damaged) {
            val initial = text?.let { mapOf(StorageFavoritesStore.KEY to it) }
            val storage = InMemoryStorage(initial.orEmpty())
            val store = StorageFavoritesStore(storage)
            assertTrue(store.favorites.value.isEmpty(), "$text")
        }
    }

    @Test
    fun unknownFieldsAndDuplicatesAreTolerated() {
        val text = """[{"varietyId":"v-01","name":"A","color":"red"},
            {"varietyId":"v-01","name":"A again"}]"""
        assertEquals(listOf(Favorite("v-01", "A")), FavoritesCodec.decode(text))
    }

    @Test
    fun encodeAndDecodeRoundTrip() {
        val list = listOf(beans, chard)
        assertEquals(list, FavoritesCodec.decode(FavoritesCodec.encode(list)))
    }
}
