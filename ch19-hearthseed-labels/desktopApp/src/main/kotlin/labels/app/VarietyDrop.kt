package labels.app

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import java.awt.datatransfer.DataFlavor
import java.io.File

/** Accepts files dropped from the file manager and passes the first one on. */
@OptIn(ExperimentalComposeUiApi::class) // awtTransferable
@Composable
fun Modifier.fileDropTarget(onFile: (File) -> Unit): Modifier {
    val target = remember(onFile) {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val data = event.awtTransferable
                val flavor = DataFlavor.javaFileListFlavor
                if (!data.isDataFlavorSupported(flavor)) return false
                val files = data.getTransferData(flavor) as List<*>
                val file = files.firstOrNull() as? File ?: return false
                onFile(file)
                return true
            }
        }
    }
    return dragAndDropTarget(shouldStartDragAndDrop = { true }, target = target)
}
