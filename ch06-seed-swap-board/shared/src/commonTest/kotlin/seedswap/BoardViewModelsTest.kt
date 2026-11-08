package seedswap

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import seedswap.board.detail.OfferDetailUiState
import seedswap.board.detail.OfferDetailViewModel
import seedswap.board.list.OfferListUiState
import seedswap.board.list.OfferListViewModel
import seedswap.core.InMemorySwapRepository
import seedswap.core.Proposal

@OptIn(ExperimentalCoroutinesApi::class)
class BoardViewModelsTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun listIsLoadingUntilObservedThenShowsOffers() = runTest {
        val viewModel = OfferListViewModel(InMemorySwapRepository())
        assertEquals(OfferListUiState.Loading, viewModel.uiState.value)

        backgroundScope.launch { viewModel.uiState.collect {} }  // a "screen"
        runCurrent()
        val state = viewModel.uiState.value
        assertEquals(5, (state as OfferListUiState.Content).offers.size)
    }

    @Test
    fun emptyBoardHasItsOwnState() = runTest {
        val viewModel = OfferListViewModel(InMemorySwapRepository(initial = emptyList()))
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
        assertEquals(OfferListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun detailFollowsTheRepository() = runTest {
        val repository = InMemorySwapRepository()
        val viewModel = OfferDetailViewModel("o2", repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        repository.propose(Proposal("o2", "Lena Moss", "Butternut squash", 15, ""))
        runCurrent()

        val state = viewModel.uiState.value as OfferDetailUiState.Content
        assertEquals(1, state.offer.proposals)
    }

    @Test
    fun unknownOfferIsNotFound() = runTest {
        val viewModel = OfferDetailViewModel("nope", InMemorySwapRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }
        runCurrent()
        assertEquals(OfferDetailUiState.NotFound, viewModel.uiState.value)
    }
}
