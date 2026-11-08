// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/theme/HearthseedTheme.kt
package hearthseed.shared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Garden greens and a seed-coat brown; dark mode follows the system.
private val Light = lightColorScheme(
    primary = Color(0xFF2F6B3A), onPrimary = Color.White,
    secondary = Color(0xFF7A5A2E), onSecondary = Color.White,
)
private val Dark = darkColorScheme(
    primary = Color(0xFF9AD5A0), onPrimary = Color(0xFF00390F),
    secondary = Color(0xFFE6C08B), onSecondary = Color(0xFF412D00),
)

@Composable
fun HearthseedTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}
