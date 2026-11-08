package seedswap

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import seedswap.core.FieldError
import seedswap.core.InMemorySwapRepository
import seedswap.core.ProposalField
import seedswap.proposal.ProposalFormViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class ProposalFormViewModelTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun invalidDraftShowsErrorsAndSendsNothing() = runTest {
        val repository = InMemorySwapRepository()
        val viewModel = ProposalFormViewModel("o4", repository)

        viewModel.onGramsChange("ten")
        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(FieldError.NOT_A_NUMBER, state.errors[ProposalField.GRAMS])
        assertEquals(FieldError.REQUIRED, state.errors[ProposalField.MEMBER])
        assertTrue(repository.proposals.value.isEmpty())

        viewModel.onGramsChange("10")             // editing clears that error
        assertEquals(null, viewModel.uiState.value.errors[ProposalField.GRAMS])
    }

    @Test
    fun validDraftIsSentOnceAndReportedAsState() = runTest {
        val repository = InMemorySwapRepository(latency = 1.seconds)
        val viewModel = ProposalFormViewModel("o4", repository)
        viewModel.onMemberChange("Lena Moss")
        viewModel.onVarietyChange("Genovese basil")
        viewModel.onGramsChange("10")

        viewModel.onSubmit()
        viewModel.onSubmit()                      // double tap: ignored
        runCurrent()
        assertTrue(viewModel.uiState.value.isSending)

        advanceUntilIdle()                        // the 1 s latency, virtually
        val state = viewModel.uiState.value
        assertFalse(state.isSending)
        assertEquals("p1", state.sentProposalId)
        assertEquals(1, repository.proposals.value.size)
        assertEquals(1_000, currentTime)
    }
}
