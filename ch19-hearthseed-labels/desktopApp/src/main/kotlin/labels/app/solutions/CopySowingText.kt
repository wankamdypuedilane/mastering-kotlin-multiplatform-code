package labels.app.solutions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.window.MenuScope
import kotlinx.coroutines.launch
import labels.app.EditorState
import labels.app.shortcut
import java.awt.datatransfer.StringSelection

/** Exercise 19.1: the sowing text as a plain-text clipboard entry. */
@OptIn(ExperimentalComposeUiApi::class)
fun sowingTextClipEntry(text: String): ClipEntry =
    ClipEntry(StringSelection(text))

/** Exercise 19.1: Edit > Copy sowing text (Ctrl/Cmd+Shift+T). */
@Composable
fun MenuScope.CopySowingTextItem(editor: EditorState) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val keys = shortcut(Key.T, shift = true)
    Item("Copy sowing text", shortcut = keys, onClick = {
        scope.launch {
            clipboard.setClipEntry(sowingTextClipEntry(editor.sowingText))
            editor.status = "Sowing text copied"
        }
    })
}
