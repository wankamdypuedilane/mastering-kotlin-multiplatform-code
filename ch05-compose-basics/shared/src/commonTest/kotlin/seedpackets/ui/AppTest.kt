package seedpackets.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppTest {
    @Test
    fun appShowsTitleFromResourcesAndTogglesTheTheme() = runComposeUiTest {
        setContent { App() }

        onNodeWithText("Seed Packet Gallery").assertExists()
        onNodeWithTag("dark-switch").assertIsOff().performClick()
        onNodeWithTag("dark-switch").assertIsOn()
    }
}
