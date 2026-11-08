// hearthseed/solutions/src/commonTest/kotlin/hearthseed/solutions/Chapter12SolutionsTest.kt
package hearthseed.solutions

import hearthseed.core.data.DataError
import hearthseed.core.data.DataResult
import hearthseed.core.data.getOrNull
import hearthseed.core.data.local.InMemoryVarietyLocalSource
import hearthseed.core.testing.FakeCatalogRepository
import hearthseed.core.testing.FakeCatalogRepository.PageRequest
import hearthseed.core.testing.SampleData
import hearthseed.feature.catalog.list.CatalogUiState
import hearthseed.feature.catalog.list.CatalogViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class Chapter12SolutionsTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun emptyMessageNamesTheFilters() {
        assertEquals("No varieties match \"pea\" in Poaceae.",
            CatalogUiState(query = " pea", family = "Poaceae").emptyMessage())
        assertEquals("No varieties match \"pea\".", CatalogUiState(query = "pea").emptyMessage())
        assertEquals("No varieties in Poaceae yet.", CatalogUiState(family = "Poaceae").emptyMessage())
        assertEquals("The catalog is empty.", CatalogUiState().emptyMessage())
    }

    @Test fun clearingBothFiltersSendsOneRequest() = runTest {
        val repository = FakeCatalogRepository()
        val viewModel = CatalogViewModel(repository)
        viewModel.onQueryChange("pea")
        viewModel.onFamilySelected("Fabaceae")
        advanceUntilIdle()
        repository.pageRequests.clear()
        viewModel.clearFilters()
        advanceUntilIdle()
        assertEquals(listOf(PageRequest("", null, 1)), repository.pageRequests)
    }

    @Test fun nearTheEndMeansWithinTheThreshold() {
        assertFalse(shouldLoadMore(lastVisibleIndex = 15, itemCount = 21))
        assertTrue(shouldLoadMore(lastVisibleIndex = 17, itemCount = 21))
        assertFalse(shouldLoadMore(lastVisibleIndex = null, itemCount = 21))
        assertFalse(shouldLoadMore(lastVisibleIndex = 0, itemCount = 0))
    }

    @Test fun offlinePagesComeFromTheDevice() = runTest {
        val remote = FakeCatalogRepository()
        val local = InMemoryVarietyLocalSource().apply { saveVarieties(SampleData.varieties) }
        val repository = OfflineFallbackCatalogRepository(remote, local)

        remote.failure = DataError.Offline
        val page = repository.varieties("larkmoor", null, page = 1, pageSize = 20).getOrNull()
        assertEquals(listOf("var-010"), page?.items?.map { it.id })
        assertTrue(repository.servedFromDevice.value)

        remote.failure = null
        repository.varieties("", null, page = 1, pageSize = 20)
        assertFalse(repository.servedFromDevice.value)
    }

    @Test fun otherErrorsAndAnEmptyDevicePassThrough() = runTest {
        val remote = FakeCatalogRepository().apply { failure = DataError.Server(503) }
        val repository = OfflineFallbackCatalogRepository(remote, InMemoryVarietyLocalSource())
        assertEquals(DataResult.Failure(DataError.Server(503)),
            repository.varieties("", null, 1, 20))
        remote.failure = DataError.Offline
        assertEquals(DataResult.Failure(DataError.Offline),
            repository.varieties("", null, 1, 20))
    }
}
