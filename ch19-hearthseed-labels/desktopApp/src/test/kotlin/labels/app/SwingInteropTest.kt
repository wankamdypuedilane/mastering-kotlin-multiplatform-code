// ch19-hearthseed-labels/desktopApp/src/test/kotlin/labels/app/SwingInteropTest.kt
package labels.app

import labels.core.SampleVarieties
import javax.swing.JComponent
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SwingInteropTest {
    @Test
    fun spinnerReportsChangesAndStaysInRange() {
        val seen = mutableListOf<Int>()
        val spinner = copiesSpinner(initial = 2) { seen += it }

        spinner.value = 5
        spinner.value = spinner.nextValue
        assertEquals(listOf(5, 6), seen)

        spinner.value = 99
        assertEquals(null, spinner.nextValue) // 99 is the maximum
    }

    @Test
    fun previewPanelIsASwingComponent() = SwingUtilities.invokeAndWait {
        // Created headlessly; it composes once added to a shown window.
        val panel = labelPreviewPanel(EditorState(SampleVarieties.all))
        assertIs<JComponent>(panel)
    }
}
