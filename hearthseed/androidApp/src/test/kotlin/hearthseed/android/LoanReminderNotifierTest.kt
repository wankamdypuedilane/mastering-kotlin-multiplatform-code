// hearthseed/androidApp/src/test/kotlin/hearthseed/android/LoanReminderNotifierTest.kt
package hearthseed.android

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import hearthseed.core.model.Loan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

// Exercise 17.3, on Android 16 (API 36) inside Robolectric.
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [Build.VERSION_CODES.BAKLAVA])
class LoanReminderNotifierTest {
    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val manager = app.getSystemService(Context.NOTIFICATION_SERVICE)
        as NotificationManager
    private val notifications = shadowOf(manager)
    private val growing = listOf(
        Loan("loan-001", "lot-013", "mem-003", "2026-04-02", 2026, null),
    )

    @Test fun withoutThePermissionNothingIsPosted() {
        assertFalse(LoanReminderNotifier(app).canNotify())
        assertFalse(LoanReminderNotifier(app).notifyOpenLoans(growing))
        assertEquals(0, notifications.allNotifications.size)
    }

    @Test fun withThePermissionOneReminderIsPosted() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        val notifier = LoanReminderNotifier(app)

        assertTrue(notifier.canNotify())
        assertTrue(notifier.notifyOpenLoans(growing))
        assertTrue(notifier.notifyOpenLoans(growing)) // replaces, same id

        val posted = notifications.allNotifications.single()
        assertEquals(LoanReminderNotifier.CHANNEL_ID, posted.channelId)
        assertEquals("You are growing 1 lot. Save seed for the 2026 return.",
            posted.extras.getString(NotificationCompat.EXTRA_TEXT))
        assertEquals("Loan reminders",
            manager.getNotificationChannel(posted.channelId).name)
    }

    @Test fun noLoansNoNotification() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        assertFalse(LoanReminderNotifier(app).notifyOpenLoans(emptyList()))
        assertEquals(0, notifications.allNotifications.size)
    }
}
