package seedpackets.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** The same three modifiers in two orders: both boxes are 80 dp wide. */
@Composable
fun ModifierOrderDemo() {
    val primary = MaterialTheme.colorScheme.primary
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            Modifier
                .testTag("padding-first")
                .padding(16.dp)         // 16 dp of empty space all around
                .background(primary)    // paints only what is left inside
                .size(48.dp),
        )
        Box(
            Modifier
                .testTag("background-first")
                .background(primary)    // paints the whole 80 dp
                .padding(16.dp)         // then insets what comes next
                .size(48.dp),
        )
    }
}
