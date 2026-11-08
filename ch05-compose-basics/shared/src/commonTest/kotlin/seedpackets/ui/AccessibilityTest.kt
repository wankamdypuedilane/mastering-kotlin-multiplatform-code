// ch05-compose-basics/shared/src/commonTest/.../ui/AccessibilityTest.kt
package seedpackets.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import seedpackets.model.SampleCatalog
import seedpackets.ui.theme.SeedTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AccessibilityTest {
    private val ember = SampleCatalog.packets.first { it.id == "tomato-ember" }

    @Test
    fun cardTextsAreReadAsOneNode() = runComposeUiTest {
        setContent { SeedTheme { SeedPacketCard(ember, false, {}, 0, {}) } }
        onNodeWithText("Ember Cherry")      // the merged node has all three
            .assertTextContains("65 days to harvest")
            .assertTextContains("4 packets left")
    }

    @Test
    fun stepperButtonsHaveTouchTargetsOf48Dp() = runComposeUiTest {
        setContent { SeedTheme { SeedPacketCard(ember, false, {}, 0, {}) } }
        val more: SemanticsMatcher = hasContentDescription("More packets") and
            hasAnyAncestor(hasTestTag("card-tomato-ember"))
        // The button draws 40 dp; Material reserves 48 dp for its touch area.
        onNode(more).assertWidthIsEqualTo(40.dp)
            .assertTouchWidthIsEqualTo(48.dp)
            .assertTouchHeightIsEqualTo(48.dp)
    }
}
