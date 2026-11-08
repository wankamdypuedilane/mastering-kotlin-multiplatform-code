// ch20-testing/ui/src/commonMain/kotlin/seedloans/ui/RemindersScreen.kt
package seedloans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import seedloans.LoanReminder
import seedloans.Reminder
import seedloans.ReminderDispatcher
import seedloans.describe

/** Stateless list of reminders with a send button. */
@Composable
fun RemindersScreen(
    reminders: List<Reminder>,
    onSend: suspend () -> Int,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var delivered by remember { mutableStateOf<Int?>(null) }
    Column(
        modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (reminders.isEmpty()) Text("Nothing to return yet")
        LazyColumn(Modifier.testTag("reminders")) {
            items(reminders, key = { it.loan.id }) { reminder ->
                ListItem(
                    headlineContent = {
                        Text("Seed lot ${reminder.loan.lotId}")
                    },
                    supportingContent = { Text(reminder.status.describe()) },
                    modifier = Modifier.testTag("reminder-${reminder.loan.id}"),
                )
            }
        }
        Button(
            onClick = { scope.launch { delivered = onSend() } },
            enabled = reminders.isNotEmpty(),
        ) { Text("Send reminders") }
        delivered?.let { Text("$it sent", Modifier.testTag("delivered")) }
    }
}

/** Wires the screen to the services; the flow is tested with Turbine. */
@Composable
fun RemindersRoute(
    memberId: String,
    reminder: LoanReminder,
    dispatcher: ReminderDispatcher,
) {
    val flow = remember(memberId) { reminder.reminders(memberId) }
    val reminders by flow.collectAsState(initial = emptyList())
    RemindersScreen(reminders, onSend = { dispatcher.sendOverdue(reminders) })
}
