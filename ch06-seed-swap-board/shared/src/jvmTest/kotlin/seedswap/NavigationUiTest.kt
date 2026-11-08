package seedswap

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import seedswap.core.InMemorySwapRepository

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class NavigationUiTest {
    // The test thread drives the UI: give viewModelScope and the
    // navigation lifecycle a Main dispatcher that runs right here.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun listToDetailToFormToConfirmationAndBack() = runComposeUiTest {
        val repository = InMemorySwapRepository()
        setContent { App(repository) }

        onNodeWithText("Red oak-leaf lettuce").performClick()
        onNodeWithText("Propose a swap").performClick()
        onNodeWithText("Send proposal").performClick()     // empty form
        onNodeWithText("Proposal sent").assertDoesNotExist()

        onNodeWithText("Your name").performTextInput("Lena Moss")
        onNodeWithText("Variety you give").performTextInput("Genovese basil")
        onNodeWithText("Grams").performTextInput("10")
        onNodeWithText("Send proposal").performClick()

        onNodeWithText("Proposal sent").assertIsDisplayed()
        assertEquals(1, repository.proposals.value.size)

        // popUpTo removed the form: one step back is the offer, not the form.
        onNodeWithText("View the offer").performClick()
        onNodeWithText("Proposals so far: 1").assertIsDisplayed()
        onNodeWithText("Send proposal").assertDoesNotExist()
    }
}
