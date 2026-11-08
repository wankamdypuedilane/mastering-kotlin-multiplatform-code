// hearthseed/feature/catalog/src/jvmTest/kotlin/hearthseed/feature/catalog/CatalogScreenUiTest.kt
package hearthseed.feature.catalog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import hearthseed.core.data.DataError
import hearthseed.core.testing.FakeCatalogRepository
import hearthseed.feature.catalog.list.CatalogRoute
import hearthseed.feature.catalog.list.CatalogViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class CatalogScreenUiTest {
    // viewModelScope uses Dispatchers.Main: run it on the test thread.
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun searchesLoadsMoreAndOpensAVariety() = runComposeUiTest {
        val repository = FakeCatalogRepository()
        val opened = mutableListOf<String>()
        val viewModel = CatalogViewModel(repository)
        setContent { CatalogRoute(viewModel, onVarietyClick = { opened += it }, onOpenLoans = {}) }

        onNodeWithTag("varieties").performScrollToNode(hasText("Load more (20 of 25)"))
        onNodeWithText("Load more (20 of 25)").performClick()
        onNodeWithTag("varieties").performScrollToNode(hasText("All shown (25)"))
        onNodeWithText("Sample variety 25").assertIsDisplayed()

        onNodeWithText("Search varieties").performTextInput("pea")
        mainClock.advanceTimeBy(500) // past the search delay
        onNodeWithText("All shown (1)").assertIsDisplayed()
        onNodeWithText("Larkmoor Pea").performClick()
        assertEquals(listOf("var-010"), opened)
    }

    @Test fun showsAnErrorWithRetry() = runComposeUiTest {
        val repository = FakeCatalogRepository().apply { failure = DataError.Offline }
        setContent { CatalogRoute(CatalogViewModel(repository), {}, {}) }

        onNodeWithText("Can't reach the seed library. Check your connection.")
            .assertIsDisplayed()
        repository.failure = null
        onNodeWithText("Retry").performClick()
        onNodeWithText("Sample variety 1").assertIsDisplayed()
    }
}
