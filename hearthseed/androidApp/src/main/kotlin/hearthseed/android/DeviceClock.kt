// hearthseed/androidApp/src/main/kotlin/hearthseed/android/DeviceClock.kt
package hearthseed.android

import android.widget.TextClock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/** An Android View inside Compose (Chapter 17): the system's TextClock. */
@Composable
fun DeviceClock(pattern: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context -> TextClock(context) }, // once, on entry
        update = { clock ->              // after factory, and on a new pattern
            clock.format12Hour = pattern // the same pattern whatever the
            clock.format24Hour = pattern // 12/24-hour system setting
        },
        modifier = modifier,
    )
}
