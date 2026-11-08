package seedswap

import androidx.lifecycle.SavedStateHandle
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import seedswap.board.list.OfferListUiState
import seedswap.core.FieldError
import seedswap.core.InMemorySwapRepository
import seedswap.core.ProposalDraft
import seedswap.core.ProposalField
import seedswap.core.ProposalResult
import seedswap.proposal.ProposalFormUiState
import seedswap.solutions.ProposalIntent
import seedswap.solutions.ProposalStore
import seedswap.solutions.SavedDraftProposalViewModel
import seedswap.solutions.SearchableOfferListViewModel
import seedswap.solutions.reduce

@OptIn(ExperimentalCoroutinesApi::class)
class SolutionsTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // Exercise 6.1
    @Test
    fun searchFiltersByVarietyIgnoringCase() = runTest {
        val viewModel = SearchableOfferListViewModel(InMemorySwapRepository())
        backgroundScope.launch { viewModel.uiState.collect {} }

        fun shown() = (viewModel.uiState.value as? OfferListUiState.Content)
            ?.offers?.map { it.id }

        runCurrent()
        assertEquals(listOf("o1", "o2", "o3", "o4", "o5"), shown())

        viewModel.onQueryChange(" BEAN ")
        runCurrent()
        assertEquals(listOf("o2", "o5"), shown())

        viewModel.onQueryChange("okra")
        runCurrent()
        assertEquals(OfferListUiState.Empty, viewModel.uiState.value)
        assertEquals("okra", viewModel.query.value)
    }

    // Exercise 6.2
    @Test
    fun draftSurvivesANewViewModelBuiltFromTheSameSavedState() = runTest {
        val repository = InMemorySwapRepository()
        val handle = SavedStateHandle()
        val first = SavedDraftProposalViewModel("o4", handle, repository)
        first.onMemberChange("Lena Moss")
        first.onGramsChange("12")

        val second = SavedDraftProposalViewModel("o4", handle, repository)
        assertEquals(
            ProposalDraft(member = "Lena Moss", grams = "12"),
            second.uiState.value.draft,
        )
        second.onVarietyChange("Genovese basil")
        second.onSubmit()
        advanceUntilIdle()
        assertEquals("p1", second.uiState.value.sentProposalId)
    }

    // Exercise 6.3: the reducer is a pure function.
    @Test
    fun reducerAsksToSendExactlyOnce() {
        var state = ProposalFormUiState()
        for ((field, text) in listOf(
            ProposalField.MEMBER to "Lena Moss",
            ProposalField.VARIETY to "Genovese basil",
            ProposalField.GRAMS to "10",
        )) {
            state = reduce(state, ProposalIntent.Edit(field, text), "o4").state
        }
        val first = reduce(state, ProposalIntent.Submit, "o4")
        assertNotNull(first.send)
        assertTrue(first.state.isSending)

        val second = reduce(first.state, ProposalIntent.Submit, "o4")
        assertNull(second.send)                         // double tap ignored

        val done = reduce(second.state, ProposalIntent.SendFinished(ProposalResult.Sent("p9")), "o4")
        assertEquals("p9", done.state.sentProposalId)
    }

    @Test
    fun reducerReportsErrorsWithoutSending() {
        val next = reduce(ProposalFormUiState(), ProposalIntent.Submit, "o4")
        assertNull(next.send)
        assertEquals(FieldError.REQUIRED, next.state.errors[ProposalField.MEMBER])
    }

    @Test
    fun storeRunsTheSendEffect() = runTest {
        val repository = InMemorySwapRepository()
        val store = ProposalStore("o4", repository)
        store.dispatch(ProposalIntent.Edit(ProposalField.MEMBER, "Lena Moss"))
        store.dispatch(ProposalIntent.Edit(ProposalField.VARIETY, "Genovese basil"))
        store.dispatch(ProposalIntent.Edit(ProposalField.GRAMS, "10"))
        store.dispatch(ProposalIntent.Submit)
        advanceUntilIdle()
        assertEquals("p1", store.uiState.value.sentProposalId)
        assertEquals(1, repository.proposals.value.size)
    }
}
