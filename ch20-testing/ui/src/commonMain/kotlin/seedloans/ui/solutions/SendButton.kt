package seedloans.ui.solutions

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch

/** Exercise 20.4: a send button that shows progress and blocks double taps. */
@Composable
fun SendRemindersButton(onSend: suspend () -> Int, enabled: Boolean = true) {
    val scope = rememberCoroutineScope()
    var sending by remember { mutableStateOf(false) }
    var delivered by remember { mutableStateOf<Int?>(null) }
    Column {
        Button(
            onClick = {
                sending = true
                scope.launch {
                    try { delivered = onSend() } finally { sending = false }
                }
            },
            enabled = enabled && !sending,
            modifier = Modifier.testTag("send"),
        ) { Text(if (sending) "Sending…" else "Send reminders") }
        delivered?.let { Text("$it sent", Modifier.testTag("delivered")) }
    }
}
