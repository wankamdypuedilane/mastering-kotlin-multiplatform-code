// hearthseed/shared/src/iosMain/kotlin/hearthseed/shared/MainViewController.kt
package hearthseed.shared

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import hearthseed.shared.di.initKoin
import hearthseed.shared.platform.IosSharer
import hearthseed.shared.platform.LocalSharer
import platform.UIKit.UIViewController

/** Swift: MainViewControllerKt.MainViewController(), in a SwiftUI view. */
fun MainViewController(): UIViewController {
    lateinit var controller: UIViewController
    controller = ComposeUIViewController {
        // Chapter 16: the Share button of the shared UI opens the iOS sheet.
        val sharer = remember { IosSharer { controller } }
        CompositionLocalProvider(LocalSharer provides sharer) { App() }
    }
    return controller
}

/** Swift: MainViewControllerKt.doInitKoinIos(), once, in the App's init. */
fun initKoinIos() = initKoin()
