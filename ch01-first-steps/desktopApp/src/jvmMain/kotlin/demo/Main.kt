package demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    // Exercise 1.2: the window title comes from the shared :core module.
    val title = packetLabel("Larkmoor Runner", 2026)
    Window(onCloseRequest = ::exitApplication, title = title) { Hello() }
}
