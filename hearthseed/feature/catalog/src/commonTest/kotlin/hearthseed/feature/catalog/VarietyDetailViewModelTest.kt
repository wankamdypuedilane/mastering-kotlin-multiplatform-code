// hearthseed/feature/catalog/src/commonTest/kotlin/hearthseed/feature/catalog/VarietyDetailViewModelTest.kt
package hearthseed.feature.catalog

import hearthseed.core.data.DataError
import hearthseed.core.model.LotStatus
import hearthseed.core.testing.FakeCatalogRepository
import hearthseed.feature.catalog.detail.VarietyDetailViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class VarietyDetailViewModelTest {
    private val repository = FakeCatalogRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun showsTheVarietyAndFollowsItsLots() = runTest {
        val viewModel = VarietyDetailViewModel("var-010", repository)
        backgroundScope.launch { viewModel.state.collect {} } // a "screen"
        advanceUntilIdle()
        assertEquals("Larkmoor Pea", viewModel.state.value.variety?.commonName)
        assertEquals(listOf(LotStatus.AVAILABLE, LotStatus.ON_LOAN),
            viewModel.state.value.lots?.map { it.status })

        repository.markOnLoan("lot-019") // a borrow on the borrow screen
        advanceUntilIdle()
        assertEquals(listOf(LotStatus.ON_LOAN, LotStatus.ON_LOAN),
            viewModel.state.value.lots?.map { it.status })
    }

    @Test fun anUnknownVarietyIsNotFound() = runTest {
        val viewModel = VarietyDetailViewModel("var-999", repository)
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        assertEquals(DataError.NotFound, viewModel.state.value.varietyError)
        assertEquals(emptyList(), viewModel.state.value.lots)
    }
}
