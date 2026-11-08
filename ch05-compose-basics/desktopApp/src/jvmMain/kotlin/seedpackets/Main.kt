package seedpackets

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import seedpackets.ui.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Seed Packet Gallery") {
        App()
    }
}
