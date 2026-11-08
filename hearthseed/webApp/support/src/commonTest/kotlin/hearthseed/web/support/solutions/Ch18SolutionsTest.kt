// hearthseed/webApp/support/src/commonTest/kotlin/hearthseed/web/support/solutions/Ch18SolutionsTest.kt
package hearthseed.web.support.solutions

import hearthseed.web.support.Favorite
import hearthseed.web.support.InMemoryStorage
import hearthseed.web.support.StorageFavoritesStore
import kotlin.test.Test
import kotlin.test.assertEquals

class Ch18SolutionsTest {
    private val a = Favorite("v-01", "Larkmoor Runner Bean")
    private val b = Favorite("v-02", "Bramble Kale")
    private val c = Favorite("v-03", "Rainbow Chard")

    @Test
    fun tabTitleCountsFavorites() {
        assertEquals("Hearthseed", tabTitle(0))
        assertEquals("Hearthseed (1 favorite)", tabTitle(1))
        assertEquals("Hearthseed (4 favorites)", tabTitle(4))
    }

    @Test
    fun sessionParameterSelectsSessionStorage() {
        assertEquals(StorageChoice.LOCAL, storageChoice(""))
        assertEquals(StorageChoice.LOCAL, storageChoice("?sessions=1"))
        assertEquals(StorageChoice.SESSION, storageChoice("?session"))
        assertEquals(StorageChoice.SESSION, storageChoice("?a=1&session=yes"))
    }

    @Test
    fun syncedStoreSeesWhatAnotherTabWrote() {
        val shared = InMemoryStorage() // both "tabs" use one localStorage
        val tab1 = SyncedFavoritesStore(shared)
        val tab2 = SyncedFavoritesStore(shared)
        tab1.toggle(a)
        assertEquals(emptyList(), tab2.favorites.value) // no event yet
        tab2.onStorageChanged("some.other.key")
        assertEquals(emptyList(), tab2.favorites.value)
        tab2.onStorageChanged(StorageFavoritesStore.KEY)
        assertEquals(listOf(a), tab2.favorites.value)
        shared.remove(StorageFavoritesStore.KEY)
        tab2.onStorageChanged(null) // localStorage.clear() in tab 1
        assertEquals(emptyList(), tab2.favorites.value)
    }

    @Test
    fun recentlyViewedIsNewestFirstWithoutDuplicatesAndCapped() {
        val storage = InMemoryStorage()
        val recent = RecentlyViewed(storage, capacity = 2)
        recent.record(a)
        recent.record(b)
        recent.record(a)
        assertEquals(listOf(a, b), recent.items.value)
        recent.record(c)
        assertEquals(listOf(c, a), recent.items.value)
        assertEquals(listOf(c, a), RecentlyViewed(storage, 2).items.value)
    }
}
