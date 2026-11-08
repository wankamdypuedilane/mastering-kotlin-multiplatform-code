// hearthseed/androidApp/src/main/kotlin/hearthseed/android/LoanReminderNotifier.kt
package hearthseed.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import hearthseed.core.model.Loan
import hearthseed.shared.platform.reminderText

/** Exercise 17.3: posts one notification about the lots a member grows. */
class LoanReminderNotifier(private val context: Context) {
    private val manager = NotificationManagerCompat.from(context)

    /** False before the runtime permission (13+) or if the member muted us. */
    fun canNotify(): Boolean =
        hasPermission() && manager.areNotificationsEnabled()

    /** Returns whether a notification was posted. */
    fun notifyOpenLoans(openLoans: List<Loan>): Boolean {
        val text = reminderText(openLoans) ?: return false
        if (!hasPermission()) return false
        // Creating an existing channel again is a no-op; since Android 8 every
        // notification needs one, and the member can mute it in the settings.
        val channelName = context.getString(R.string.reminder_channel_name)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT,
            ).setName(channelName).build(),
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            return false // permission revoked between the check and the call
        }
        return true
    }

    private fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val CHANNEL_ID = "loan-reminders"
        const val NOTIFICATION_ID = 17
    }
}
