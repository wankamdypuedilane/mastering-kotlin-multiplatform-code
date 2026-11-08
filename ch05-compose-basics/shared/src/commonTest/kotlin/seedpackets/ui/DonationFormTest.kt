// ch05-compose-basics/shared/src/commonTest/.../ui/DonationFormTest.kt
package seedpackets.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import seedpackets.ui.theme.SeedTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DonationFormTest {
    @Test
    fun ruleAcceptsOneToFiveHundred() {
        assertTrue(isValidGrams("1") && isValidGrams("500"))
        assertFalse(isValidGrams("0") || isValidGrams("501"))
        assertFalse(isValidGrams(""))
    }

    @Test
    fun fieldKeepsDigitsAndFlagsOutOfRange() = runComposeUiTest {
        var donated: Int? = null
        setContent { SeedTheme { DonationForm(onDonate = { donated = it }) } }
        val donate = onNodeWithText("Donate")
        donate.assertIsNotEnabled()                  // empty: nothing to send

        onNodeWithTag("grams").performTextInput("9x00")
        onNodeWithTag("grams").assertTextContains("900")     // 'x' dropped
        onNodeWithText("Enter 1 to 500 grams").assertExists()
        donate.assertIsNotEnabled()

        onNodeWithTag("grams").performTextClearance()
        onNodeWithTag("grams").performTextInput("25")
        onNodeWithText("Enter 1 to 500 grams").assertDoesNotExist()
        donate.assertIsEnabled().performClick()
        assertEquals(25, donated)
    }
}
