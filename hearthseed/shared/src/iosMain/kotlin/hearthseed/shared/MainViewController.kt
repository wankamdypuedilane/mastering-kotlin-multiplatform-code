// hearthseed/shared/src/iosMain/kotlin/hearthseed/shared/MainViewController.kt
package hearthseed.shared

import androidx.compose.ui.window.ComposeUIViewController
import hearthseed.shared.di.initKoin
import platform.UIKit.UIViewController

/** Swift: MainViewControllerKt.MainViewController(), in a SwiftUI view. */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }

/** Swift: MainViewControllerKt.doInitKoinIos(), once, in the App's init. */
fun initKoinIos() = initKoin()
