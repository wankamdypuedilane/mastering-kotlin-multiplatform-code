// mendfield/shared/src/iosMain/kotlin/mendfield/shared/MainViewController.kt
package mendfield.shared

import androidx.compose.ui.window.ComposeUIViewController
import mendfield.shared.di.initKoin
import mendfield.shared.today.TodayFeed
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

// The iOS-facing API of the framework, as top-level functions: Swift calls
// them as MainViewControllerKt.startMendfield() and so on.

/** Once, in the SwiftUI App's init: Koin, Room and the sync loop. */
fun startMendfield() {
    initKoin()
}

/** The Compose organizer app, for a UIViewControllerRepresentable. */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }

/** The data of the native SwiftUI "Today" screen. */
fun todayFeed(): TodayFeed = KoinPlatform.getKoin().get()
