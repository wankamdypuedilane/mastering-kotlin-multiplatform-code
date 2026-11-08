// ch06-seed-swap-board/shared/src/commonTest/.../MemberSessionTest.kt
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
import seedswap.core.InMemorySwapRepository
import seedswap.proposal.ProposalFormViewModel
import seedswap.session.BoardHeaderViewModel
import seedswap.session.MemberSession

@OptIn(ExperimentalCoroutinesApi::class)
class MemberSessionTest {
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun oneSessionFeedsSeveralViewModels() = runTest {
        val session = MemberSession()
        val header = BoardHeaderViewModel(session)
        val form = ProposalFormViewModel("o4", InMemorySwapRepository(), session)
        backgroundScope.launch { header.greeting.collect {} }
        runCurrent()
        assertEquals("Browsing as a guest", header.greeting.value)

        session.signIn("  Lena Moss ")
        runCurrent()
        assertEquals("Signed in as Lena Moss", header.greeting.value)
        assertEquals("Lena Moss", form.uiState.value.draft.member)
    }

    @Test
    fun aTypedNameIsNotOverwritten() = runTest {
        val session = MemberSession()
        val form = ProposalFormViewModel("o4", InMemorySwapRepository(), session)
        form.onMemberChange("Ada Fern")
        session.signIn("Lena Moss")
        runCurrent()
        assertEquals("Ada Fern", form.uiState.value.draft.member)
    }
}
