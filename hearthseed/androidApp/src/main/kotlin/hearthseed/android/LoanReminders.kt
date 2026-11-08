// hearthseed/androidApp/src/main/kotlin/hearthseed/android/LoanReminders.kt
package hearthseed.android

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import hearthseed.core.model.Loan
import hearthseed.shared.platform.LoanReminders

/** Asks for POST_NOTIFICATIONS when the member taps "Remind me". */
@Composable
fun rememberLoanReminders(): LoanReminders {
    val context = LocalContext.current
    val notifier = remember(context) { LoanReminderNotifier(context) }
    // The loans waiting for the answer of the permission dialog.
    val pending = remember { mutableListOf<Loan>() }
    val launcher = rememberLauncherForActivityResult(
        RequestPermission(),
    ) { granted ->
        if (granted) notifier.notifyOpenLoans(pending.toList())
        pending.clear() // denied: no notification, and no second dialog now
    }
    return remember(notifier, launcher) {
        LoanReminders { openLoans ->
            when {
                notifier.canNotify() -> notifier.notifyOpenLoans(openLoans)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    pending.clear()
                    pending += openLoans
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                // Below Android 13 canNotify() is false only if the member
                // turned notifications off in the system settings.
            }
        }
    }
}
