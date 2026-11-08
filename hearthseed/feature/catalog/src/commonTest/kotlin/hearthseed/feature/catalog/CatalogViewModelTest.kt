// hearthseed/feature/catalog/src/commonTest/kotlin/hearthseed/feature/catalog/CatalogViewModelTest.kt
package hearthseed.feature.catalog

import hearthseed.core.data.DataError
import hearthseed.core.testing.FakeCatalogRepository
import hearthseed.core.testing.FakeCatalogRepository.PageRequest
import hearthseed.feature.catalog.list.CatalogViewModel
import hearthseed.feature.catalog.list.ListState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    private val repository = FakeCatalogRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun CatalogViewModel.items() = assertIs<ListState.Items>(state.value.list)

    @Test fun showsLoadingThenTheFirstPage() = runTest {
        repository.latency = 1.seconds
        val viewModel = CatalogViewModel(repository)
        assertEquals(ListState.Loading, viewModel.state.value.list)
        advanceUntilIdle()
        val items = viewModel.items()
        assertEquals(20, items.varieties.size)
        assertEquals(25, items.total)
        assertEquals(true, items.hasMore)
    }

    @Test fun loadMoreAppendsTheNextPageUntilTheEnd() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()
        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(25, viewModel.items().varieties.size)
        assertEquals(false, viewModel.items().hasMore)
        viewModel.loadMore() // nothing left: no request
        advanceUntilIdle()
        assertEquals(listOf(1, 2), repository.pageRequests.map { it.page })
    }

    @Test fun typingSendsOneRequestAfterThePause() = runTest {
        val viewModel = CatalogViewModel(repository, 300.milliseconds)
        advanceUntilIdle()
        viewModel.onQueryChange("l")
        advanceTimeBy(100.milliseconds)
        viewModel.onQueryChange("la")
        advanceTimeBy(100.milliseconds)
        viewModel.onQueryChange("larkmoor")
        advanceUntilIdle()
        assertEquals(
            listOf(PageRequest("", null, 1), PageRequest("larkmoor", null, 1)),
            repository.pageRequests,
        )
        val found = viewModel.items().varieties.single()
        assertEquals("Larkmoor Pea", found.commonName)
    }

    @Test fun aNewFilterCancelsTheSlowRequestAndCanBeEmpty() = runTest {
        repository.latency = 1.seconds
        val viewModel = CatalogViewModel(repository)
        viewModel.onFamilySelected("Poaceae") // before the first page arrives
        advanceUntilIdle()
        assertEquals(ListState.Empty, viewModel.state.value.list)
        assertEquals("Poaceae", repository.pageRequests.last().family)
    }

    @Test fun aFailedFirstLoadShowsAnErrorUntilRetry() = runTest {
        repository.failure = DataError.Offline
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()
        assertEquals(ListState.Error(DataError.Offline), viewModel.state.value.list)
        repository.failure = null
        viewModel.retry()
        advanceUntilIdle()
        assertEquals(20, viewModel.items().varieties.size)
    }

    @Test fun aFailedRefreshKeepsTheItems() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()
        repository.failure = DataError.Server(503)
        viewModel.refresh()
        assertEquals(true, viewModel.state.value.isRefreshing)
        advanceUntilIdle()
        assertEquals(20, viewModel.items().varieties.size)
        assertEquals(false, viewModel.state.value.isRefreshing)
        assertEquals(DataError.Server(503), viewModel.state.value.refreshError)
    }

    @Test fun aFailedLoadMoreCanBeRetried() = runTest {
        val viewModel = CatalogViewModel(repository)
        advanceUntilIdle()
        repository.failure = DataError.Offline
        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(DataError.Offline, viewModel.items().loadMoreError)
        assertEquals(20, viewModel.items().varieties.size)
        repository.failure = null
        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(25, viewModel.items().varieties.size)
    }
}
