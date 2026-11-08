package labels.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import labels.core.SampleVarieties
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class LabelEditorTest {
    @Test
    fun selectingAVarietyUpdatesTextAndPreview() = runComposeUiTest {
        val state = EditorState(SampleVarieties.all)
        setContent { MaterialTheme { LabelEditor(state) } }

        onNodeWithTag("variety-var-023").performClick().assertIsSelected()
        assertEquals("Sow Mar–Sep · ready in 28 days", state.sowingText)

        onNodeWithTag("sowing-text")
            .performTextReplacement("Sow thinly, little and often")
        onNode(hasText("Sow thinly, little and often") and
            hasAnyAncestor(hasTestTag("preview"))).assertExists()
        assertEquals("firebrand-radish-70x40.png", state.design.fileName)
    }

    @Test
    fun emptyTextShowsTheProblem() = runComposeUiTest {
        val state = EditorState(SampleVarieties.all)
        setContent { MaterialTheme { LabelEditor(state) } }

        onNodeWithTag("sowing-text").performTextReplacement("")
        onNodeWithText("Sowing text is empty").assertExists()
    }

    @Test
    fun arrowKeysMoveTheSelection() = runComposeUiTest {
        val state = EditorState(SampleVarieties.all)
        setContent { MaterialTheme { LabelEditor(state) } }

        onNodeWithTag("variety-list").requestFocus()
        onNodeWithTag("variety-list").performKeyInput {
            pressKey(Key.DirectionDown)
            pressKey(Key.DirectionDown)
        }
        assertEquals("var-011", state.selected.id)
    }
}
