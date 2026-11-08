// ch20-testing/ui/src/commonTest/kotlin/seedloans/ui/solutions/SendButtonTest.kt
package seedloans.ui.solutions

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SendButtonTest {
    @Test
    fun theButtonIsBusyUntilTheSendFinishes() = runComposeUiTest {
        val gate = CompletableDeferred<Int>()
        var calls = 0
        setContent { SendRemindersButton(onSend = { calls++; gate.await() }) }
        onNodeWithTag("send").performClick()
        onNodeWithText("Sending…").assertExists()
        onNodeWithTag("send").assertIsNotEnabled().performClick()  // ignored
        gate.complete(2)
        waitUntil { onAllNodes(hasTestTag("delivered"))
            .fetchSemanticsNodes().isNotEmpty() }
        onNodeWithTag("delivered").assertTextEquals("2 sent")
        onNodeWithTag("send").assertIsEnabled()
        assertEquals(1, calls)
    }
}
