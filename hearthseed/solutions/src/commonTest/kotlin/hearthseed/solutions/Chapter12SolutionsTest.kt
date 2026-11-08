// hearthseed/solutions/src/commonTest/kotlin/hearthseed/solutions/Chapter12SolutionsTest.kt
package hearthseed.solutions

import hearthseed.core.testing.FakeCatalogRepository
import hearthseed.core.testing.FakeCatalogRepository.PageRequest
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
}
