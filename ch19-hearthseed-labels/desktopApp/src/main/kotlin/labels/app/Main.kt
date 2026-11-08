package labels.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.TrayState
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch
import labels.core.SampleVarieties
import labels.core.parseVarieties

fun main() = application {
    val editor = remember { EditorState(SampleVarieties.all) }
    val trayState = rememberTrayState()
    var showAbout by remember { mutableStateOf(false) }
    var showLargePreview by remember { mutableStateOf(false) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Hearthseed Labels — ${editor.selected.commonName}",
        icon = SeedIcon,
        state = rememberWindowState(size = DpSize(960.dp, 600.dp)),
    ) {
        EditorMenuBar(editor, trayState,
            onLargePreview = { showLargePreview = true },
            onAbout = { showAbout = true }, onQuit = ::exitApplication)
        MaterialTheme {
            LabelEditor(editor, Modifier.fileDropTarget { open(editor, it) })
        }
    }
    if (showLargePreview) {                          // a second window
        Window(onCloseRequest = { showLargePreview = false },
            title = "Preview — ${editor.design.fileName}") {
            SeedLabel(editor.design, Modifier.fillMaxSize())
        }
    }
    if (showAbout) {
        DialogWindow(onCloseRequest = { showAbout = false }, title = "About") {
            AboutContent()
        }
    }
    if (isTraySupported) {
        Tray(icon = SeedIcon, state = trayState, tooltip = "Hearthseed Labels",
            menu = { Item("Quit", onClick = ::exitApplication) })
    }
}

@OptIn(ExperimentalComposeUiApi::class) // ClipEntry(Transferable)
@Composable
private fun FrameWindowScope.EditorMenuBar(
    editor: EditorState,
    trayState: TrayState,
    onLargePreview: () -> Unit,
    onAbout: () -> Unit,
    onQuit: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    MenuBar {
        Menu("File", mnemonic = 'F') {
            Item("Open varieties…", shortcut = shortcut(Key.O), onClick = {
                chooseFileToOpen(window, "csv")?.let { open(editor, it) }
            })
            Item("Export PNG…", shortcut = shortcut(Key.E),
                enabled = editor.design.problem == null, onClick = {
                    val design = editor.design
                    val file = chooseFileToSave(window, design.fileName)
                        ?: return@Item
                    file.writeBytes(renderLabelPng(design))
                    editor.status = "Exported ${file.name}"
                    trayState.sendNotification(
                        Notification("Label exported", file.absolutePath))
                    revealInFileManager(file)
                })
            Separator()
            Item("Quit", shortcut = shortcut(Key.Q), onClick = onQuit)
        }
        Menu("Edit", mnemonic = 'E') {
            Item("Copy label image", shortcut = shortcut(Key.C, shift = true),
                enabled = editor.design.problem == null, onClick = {
                    val png = renderLabelPng(editor.design)
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(PngSelection(png)))
                        editor.status = "Label copied to the clipboard"
                    }
                })
        }
        Menu("View", mnemonic = 'V') {
            Item("Large preview", shortcut = shortcut(Key.P),
                onClick = onLargePreview)
        }
        Menu("Help", mnemonic = 'H') {
            Item("Seed library website", onClick = {
                uriHandler.openUri("https://example.org/larkmoor-seed-library")
            })
            Item("About Hearthseed Labels", onClick = onAbout)
        }
    }
}

/** Reads a CSV variety list and reports what happened in the status line. */
fun open(editor: EditorState, file: java.io.File) {
    val result = parseVarieties(file.readText())
    editor.load(result.varieties)
    val skipped = result.errors.size
    editor.status = "Loaded ${result.varieties.size} varieties" +
        if (skipped == 0) "" else ", $skipped lines skipped"
}

@Composable
private fun AboutContent() {
    Column(Modifier.padding(24.dp), Arrangement.spacedBy(8.dp)) {
        Text("Hearthseed Labels 1.0.0",
            style = MaterialTheme.typography.titleMedium)
        Text("Seed-packet labels for the Larkmoor Commons Seed Library.")
    }
}

/** A tiny vector icon for the window and the tray. */
object SeedIcon : Painter() {
    override val intrinsicSize = Size(64f, 64f)
    override fun DrawScope.onDraw() {
        drawOval(Color(0xFF2E7D32))
    }
}
