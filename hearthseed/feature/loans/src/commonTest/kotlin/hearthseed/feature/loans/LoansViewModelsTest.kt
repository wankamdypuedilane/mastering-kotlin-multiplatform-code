// hearthseed/feature/loans/src/commonTest/kotlin/hearthseed/feature/loans/LoansViewModelsTest.kt
package hearthseed.feature.loans

import hearthseed.core.data.DataError
import hearthseed.core.testing.FakeLoanRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class LoansViewModelsTest {
    private val repository = FakeLoanRepository()

    @BeforeTest fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun borrowGoesThroughSendingAndIgnoresDoubleTaps() = runTest {
        repository.latency = 1.seconds
        val viewModel = BorrowViewModel("lot-019", repository)
        viewModel.borrow()
        viewModel.borrow() // a second tap while sending
        runCurrent()
        assertEquals(BorrowUiState.Sending, viewModel.state.value)
        advanceUntilIdle()
        val borrowed = assertIs<BorrowUiState.Borrowed>(viewModel.state.value)
        assertEquals("lot-019", borrowed.loan.lotId)
        viewModel.borrow() // and one after the success
        advanceUntilIdle()
        assertEquals(1, repository.loans.size)
    }

    @Test fun aLotTakenByAnotherMemberCannotBeRetried() = runTest {
        repository.takenLots += "lot-019"
        val viewModel = BorrowViewModel("lot-019", repository)
        viewModel.borrow()
        advanceUntilIdle()
        val failed = assertIs<BorrowUiState.Failed>(viewModel.state.value)
        assertEquals(DataError.LotUnavailable, failed.error)
        assertEquals(false, failed.canRetry)
    }

    @Test fun anOfflineBorrowCanBeRetried() = runTest {
        repository.failure = DataError.Offline
        val viewModel = BorrowViewModel("lot-019", repository)
        viewModel.borrow()
        advanceUntilIdle()
        assertTrue(assertIs<BorrowUiState.Failed>(viewModel.state.value).canRetry)
        repository.failure = null
        viewModel.borrow()
        advanceUntilIdle()
        assertIs<BorrowUiState.Borrowed>(viewModel.state.value)
    }

    @Test fun myLoansSplitsOpenAndReturned() = runTest {
        repository.borrow("lot-001")
        repository.loans += repository.loans.first()
            .copy(id = "loan-old", lotId = "lot-010", returnedGrams = 40)
        val viewModel = MyLoansViewModel(repository)
        advanceUntilIdle()
        val loans = assertIs<MyLoansUiState.Loans>(viewModel.state.value)
        assertEquals(listOf("lot-001"), loans.open.map { it.lotId })
        assertEquals(listOf("lot-010"), loans.returned.map { it.lotId })
    }

    @Test fun noLoansIsItsOwnState() = runTest {
        val viewModel = MyLoansViewModel(repository)
        advanceUntilIdle()
        assertEquals(MyLoansUiState.Empty, viewModel.state.value)
    }
}
