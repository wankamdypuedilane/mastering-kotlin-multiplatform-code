// hearthseed/feature/catalog/src/commonMain/kotlin/hearthseed/feature/catalog/FamilyBadge.kt
package hearthseed.feature.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hearthseed.core.model.BotanicalFamilies

// Light tints, so that dark text on them stays readable (contrast > 4.5:1).
private val familyTints = listOf(
    Color(0xFFF6C9C2), Color(0xFFD5E8C4), Color(0xFFF9E1A8), Color(0xFFCFE3EE),
    Color(0xFFF3D2B5), Color(0xFFE4D5F0), Color(0xFFF0C9D9), Color(0xFFE0E6B8),
    Color(0xFFE8DCC8), Color(0xFFC9E8DF),
)

/** The tint of a family: stable, and different for the ten known families. */
fun familyTint(family: String): Color {
    val index = BotanicalFamilies.all.indexOf(family)
    val slot = if (index >= 0) index
        else family.hashCode().mod(familyTints.size)
    return familyTints[slot]
}

/**
 * The picture of a variety. The API has no images, so the app draws a
 * placeholder from data it has: the family's tint and two letters.
 * It is decorative; the row's text already names the family.
 */
@Composable
fun FamilyBadge(
    family: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier
            .size(size)
            .background(familyTint(family), CircleShape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            family.take(2),
            color = Color(0xFF1F1F1F),
            style = if (size >= 64.dp) MaterialTheme.typography.headlineSmall
                else MaterialTheme.typography.titleSmall,
        )
    }
}
