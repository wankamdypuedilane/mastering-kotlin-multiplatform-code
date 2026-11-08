// hearthseed/androidApp/src/test/kotlin/hearthseed/android/AndroidSharerTest.kt
package hearthseed.android

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

// Exercise 17.1. A plain Application: these tests do not need Koin.
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class AndroidSharerTest {
    @Test fun sharesPlainTextThroughTheChooser() {
        val activity =
            Robolectric.buildActivity(Activity::class.java).setup().get()
        AndroidSharer(activity).share("Larkmoor Pea (Pisum sativum)")

        val chooser = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals(0, chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
        @Suppress("DEPRECATION") // the typed overload needs API 33
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertEquals("Larkmoor Pea (Pisum sativum)",
            send.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test fun startsANewTaskFromTheApplicationContext() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        AndroidSharer(app).share("Larkmoor Pea")

        val chooser = shadowOf(app).nextStartedActivity
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK,
            chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
