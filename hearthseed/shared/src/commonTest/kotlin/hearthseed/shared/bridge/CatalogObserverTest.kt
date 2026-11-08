// hearthseed/shared/src/commonTest/kotlin/hearthseed/shared/bridge/CatalogObserverTest.kt
package hearthseed.shared.bridge

import hearthseed.core.data.DataError
import hearthseed.core.testing.FakeCatalogRepository
import hearthseed.feature.catalog.detail.VarietyDetailViewModel
import hearthseed.feature.catalog.list.CatalogViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

// Runs on the JVM, the Android host and the iOS simulator (commonTest).
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogObserverTest {
    private val repository = FakeCatalogRepository()
    private val seen = mutableListOf<CatalogSnapshot>()

    @BeforeTest fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun observer() =
        CatalogObserver { CatalogViewModel(repository, Duration.ZERO) }

    @Test fun deliversLoadingThenTheFirstPage() = runTest {
        repository.latency = 1.seconds
        val observer = observer()
        val subscription = observer.observe { seen += it }
        advanceUntilIdle()
        assertEquals(listOf(CatalogPhase.LOADING, CatalogPhase.LOADED),
            seen.map { it.phase })
        val page = seen.last()
        assertEquals(20, page.varieties.size)
        assertEquals(25, page.total)
        assertTrue(page.canLoadMore)
        assertNull(page.message)
        assertTrue(subscription.isActive)
        observer.close()
    }

    @Test fun anErrorBecomesAMessageAndRetryRecovers() = runTest {
        repository.failure = DataError.Offline
        val observer = observer()
        observer.observe { seen += it }
        advanceUntilIdle()
        assertEquals(CatalogPhase.FAILED, seen.last().phase)
        assertEquals("Can't reach the seed library. Check your connection.",
            seen.last().message)
        repository.failure = null
        observer.retry()
        advanceUntilIdle()
        assertEquals(CatalogPhase.LOADED, seen.last().phase)
        observer.close()
    }

    @Test fun anUnknownFamilyThrowsAndAKnownOneFilters() = runTest {
        val observer = observer()
        observer.observe { seen += it }
        advanceUntilIdle()
        assertFailsWith<IllegalArgumentException> {
            observer.selectFamily("Cactaceae")
        }
        observer.selectFamily("Fabaceae")
        advanceUntilIdle()
        assertEquals("Fabaceae", seen.last().family)
        assertTrue(seen.last().varieties.all { it.family == "Fabaceae" })
        observer.close()
    }

    @Test fun refreshReturnsWhenTheNewPageIsThere() = runTest {
        val observer = observer()
        observer.observe { seen += it }
        advanceUntilIdle()
        repository.varieties = repository.varieties.take(3)
        observer.refresh() // suspends until the refresh has finished
        assertEquals(3, seen.last().total)
        assertEquals(2, repository.pageRequests.size)
        observer.close()
    }

    @Test fun closeStopsEveryObservation() = runTest {
        val observer = observer()
        val subscription = observer.observe { seen += it }
        advanceUntilIdle()
        val count = seen.size
        observer.close()
        observer.search("pea") // the ViewModel is cleared: nothing happens
        advanceUntilIdle()
        assertFalse(subscription.isActive)
        assertEquals(count, seen.size)
    }

    @Test fun detailShowsTheVarietyAndItsLots() = runTest {
        val observer = VarietyDetailObserver {
            VarietyDetailViewModel("var-010", repository)
        }
        val details = mutableListOf<VarietyDetailSnapshot>()
        observer.observe { details += it }
        advanceUntilIdle()
        assertTrue(details.first().isLoading)
        val last = details.last()
        assertEquals("Larkmoor Pea", last.variety?.commonName)
        assertEquals(listOf("lot-019", "lot-020"), last.lots.map { it.id })
        assertFalse(last.isLoading)
        observer.close()
    }

    @Test fun anUnknownVarietyGivesTheNotFoundMessage() = runTest {
        val observer = VarietyDetailObserver {
            VarietyDetailViewModel("var-999", repository)
        }
        val details = mutableListOf<VarietyDetailSnapshot>()
        observer.observe { details += it }
        advanceUntilIdle()
        assertEquals("This item no longer exists in the library.",
            details.last().message)
        assertNull(details.last().variety)
        observer.close()
    }
}
