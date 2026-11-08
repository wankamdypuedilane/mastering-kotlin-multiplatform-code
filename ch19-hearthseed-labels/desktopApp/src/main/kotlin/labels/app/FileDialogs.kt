package labels.app

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/** Native "open" dialog. Blocks until the user closes it. */
fun chooseFileToOpen(parent: Frame, extension: String): File? {
    val dialog = FileDialog(parent, "Open varieties", FileDialog.LOAD).apply {
        // The filter works on macOS and Linux; Windows ignores it.
        setFilenameFilter { _, name -> name.endsWith(".$extension") }
        isVisible = true
    }
    return dialog.file?.let { File(dialog.directory, it) }
}

/** Native "save" dialog, prefilled with [suggestedName]. */
fun chooseFileToSave(parent: Frame, suggestedName: String): File? {
    val dialog = FileDialog(parent, "Export label", FileDialog.SAVE).apply {
        file = suggestedName
        isVisible = true
    }
    return dialog.file?.let { File(dialog.directory, it) }
}
