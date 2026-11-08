// hearthseed/androidApp/src/main/kotlin/hearthseed/android/AndroidSharer.kt
package hearthseed.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import hearthseed.shared.platform.Sharer

/** Opens the Android share sheet with plain text. */
class AndroidSharer(private val context: Context) : Sharer {
    override fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        // The system chooser, so the member picks an app every time.
        val chooser = Intent.createChooser(send, null)
        // Outside an activity (application Context), a new task is required.
        if (context !is Activity) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
