package seedpackets

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import seedpackets.ui.App

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
