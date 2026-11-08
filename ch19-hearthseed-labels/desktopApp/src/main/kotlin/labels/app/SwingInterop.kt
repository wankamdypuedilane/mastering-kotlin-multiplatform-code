// ch19-hearthseed-labels/desktopApp/src/main/kotlin/labels/app/SwingInterop.kt
package labels.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.SwingUtilities

/** A plain Swing spinner for 1 to 99 copies; [onChange] gets each new value. */
fun copiesSpinner(initial: Int, onChange: (Int) -> Unit): JSpinner =
    JSpinner(SpinnerNumberModel(initial, 1, 99, 1)).apply {
        addChangeListener { onChange(value as Int) }
    }

/** Swing inside Compose: the spinner becomes a composable. */
@Composable
fun CopiesSpinner(copies: Int, onChange: (Int) -> Unit) {
    val latest by rememberUpdatedState(onChange)
    SwingPanel(
        modifier = Modifier.size(96.dp, 32.dp),
        factory = { copiesSpinner(copies) { latest(it) } }, // created once
        update = { if (it.value != copies) it.value = copies }, // state changed
    )
}

/** Compose inside Swing: the live label preview as a Swing component. */
fun labelPreviewPanel(editor: EditorState): ComposePanel =
    ComposePanel().apply {
        setContent { SeedLabel(editor.design, Modifier.fillMaxSize()) }
    }

/** An existing Swing tool can host the preview next to its own components. */
fun showSwingPreviewFrame(editor: EditorState) = SwingUtilities.invokeLater {
    JFrame("Label preview (Swing host)").apply {
        add(labelPreviewPanel(editor), BorderLayout.CENTER)
        add(JLabel(editor.design.fileName), BorderLayout.SOUTH)
        setSize(420, 300)
        isVisible = true
    }
}
