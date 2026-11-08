// hearthseed/shared/src/commonTest/kotlin/hearthseed/shared/bridge/ExercisesTest.kt
package hearthseed.shared.bridge

import hearthseed.core.data.DataError
import hearthseed.core.testing.FakeCatalogRepository
import hearthseed.core.testing.FakeLoanRepository
import hearthseed.feature.catalog.list.CatalogViewModel
import hearthseed.feature.loans.MyLoansViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisesTest {
    private val catalog = FakeCatalogRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun catalogObserver() =
        CatalogObserver { CatalogViewModel(catalog, Duration.ZERO) }

    @Test fun countLabelFollowsThePages() = runTest { // Exercise 16.2
        val observer = catalogObserver()
        val seen = mutableListOf<CatalogSnapshot>()
        observer.observe { seen += it }
        assertEquals("", seen.lastOrNull()?.countLabel().orEmpty())
        advanceUntilIdle()
        assertEquals("20 of 25 varieties", seen.last().countLabel())
        observer.loadMore()
        advanceUntilIdle()
        assertEquals("25 varieties", seen.last().countLabel())
        observer.close()
    }

    @Test fun myLoansSplitsOpenAndReturned() = runTest { // Exercise 16.3
        val loans = FakeLoanRepository(catalog)
        loans.borrow("lot-019")
        loans.latency = 1.seconds // lets the test see the Loading state
        loans.loans += loans.loans.first()
            .copy(id = "loan-old", returnedGrams = 40)
        val observer = MyLoansObserver { MyLoansViewModel(loans) }
        val seen = mutableListOf<MyLoansSnapshot>()
        observer.observe { seen += it }
        advanceUntilIdle()
        assertEquals(true, seen.first().isLoading)
        assertEquals(listOf("loan-100"), seen.last().open.map { it.id })
        assertEquals(listOf("loan-old"), seen.last().returned.map { it.id })
        loans.failure = DataError.Unauthorized
        observer.refresh()
        advanceUntilIdle()
        assertEquals("Your member token was refused. Sign in again.",
            seen.last().message)
        observer.close()
    }

    @Test fun flowWatcherDeliversAndStops() = runTest { // Exercise 16.4
        catalog.latency = 1.seconds
        val observer = catalogObserver()
        val phases = mutableListOf<CatalogPhase>()
        val subscription = observer.snapshots().watch { phases += it.phase }
        advanceUntilIdle()
        assertEquals(listOf(CatalogPhase.LOADING, CatalogPhase.LOADED), phases)
        subscription.close()
        observer.retry()
        advanceUntilIdle()
        assertEquals(2, phases.size) // closed: no LOADING, LOADED again
        assertFalse(subscription.isActive)
        observer.close()
    }
}
