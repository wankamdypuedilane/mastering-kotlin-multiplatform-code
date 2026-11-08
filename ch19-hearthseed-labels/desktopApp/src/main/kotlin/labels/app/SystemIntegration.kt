package labels.app

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import java.awt.Desktop
import java.io.File

val isMacOs: Boolean = System.getProperty("os.name").startsWith("Mac")

/** Cmd on macOS, Ctrl elsewhere: the platform's own shortcut convention. */
fun shortcut(key: Key, shift: Boolean = false) =
    KeyShortcut(key, ctrl = !isMacOs, meta = isMacOs, shift = shift)

/** Shows [file] in the system file manager, if the platform allows it. */
fun revealInFileManager(file: File): Boolean {
    if (!Desktop.isDesktopSupported()) return false
    val desktop = Desktop.getDesktop()
    if (!desktop.isSupported(Desktop.Action.OPEN)) return false
    desktop.open(file.parentFile)
    return true
}
