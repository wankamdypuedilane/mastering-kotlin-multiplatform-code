// ch21-production/ui/src/commonMain/kotlin/hearthseed/ui/LotList.kt
package hearthseed.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

/** A mutable property makes the class unstable for the Compose compiler. */
class LotDraft(val id: String, val variety: String, var grams: Int)

/** Interface types such as List are unknown to the compiler: unstable. */
data class LotSummary(
    val id: String,
    val variety: String,
    val tags: List<String>,
)

/** A promise to the compiler: no property of this class ever changes. */
@Immutable
data class LotRow(val id: String, val variety: String, val tags: List<String>)

@Composable
fun DraftLine(draft: LotDraft) {
    BasicText("${draft.variety}: ${draft.grams} g")
}

@Composable
fun SummaryLine(summary: LotSummary) {
    BasicText("${summary.variety} (${summary.tags.joinToString()})")
}

@Composable
fun LotLine(row: LotRow, onOpen: (String) -> Unit) {
    BasicText(row.variety, Modifier.clickable { onOpen(row.id) })
}

@Composable
fun LotList(rows: List<LotRow>, onOpen: (String) -> Unit) {
    LazyColumn {
        // A stable key keeps each item's state with its lot when rows move.
        items(rows, key = { it.id }) { row -> LotLine(row, onOpen) }
    }
}
