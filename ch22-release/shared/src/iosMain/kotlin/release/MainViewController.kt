// ch22-release/shared/src/iosMain/kotlin/release/MainViewController.kt
package release

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { AboutScreen() }
