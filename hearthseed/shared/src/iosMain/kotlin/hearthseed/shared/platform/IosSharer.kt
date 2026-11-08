// hearthseed/shared/src/iosMain/kotlin/hearthseed/shared/platform/IosSharer.kt
package hearthseed.shared.platform

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

/** The iOS share sheet (Chapter 16), the counterpart of AndroidSharer. */
class IosSharer(private val host: () -> UIViewController) : Sharer {
    override fun share(text: String) {
        val presenter = host() // the controller that shows the Compose UI
        val sheet = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        // On iPad the sheet is a popover and needs an anchor view.
        sheet.popoverPresentationController?.sourceView = presenter.view
        presenter.presentViewController(
            sheet, animated = true, completion = null,
        )
    }
}
