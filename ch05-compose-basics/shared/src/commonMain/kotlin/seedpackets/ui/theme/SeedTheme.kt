package seedpackets.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

// Brand colors are defined once, here; components read roles such as
// colorScheme.primary and never these values directly.
private val LightColors = lightColorScheme(
    primary = Color(0xFF3F6B2A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC0F0A2),
    onPrimaryContainer = Color(0xFF0B2100),
    secondary = Color(0xFF7A5A2E),
    surface = Color(0xFFFAFAEF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5D488),
    onPrimary = Color(0xFF173800),
    primaryContainer = Color(0xFF285114),
    onPrimaryContainer = Color(0xFFC0F0A2),
    secondary = Color(0xFFEBC08B),
    surface = Color(0xFF12140E),
)

private val SeedTypography = Typography().run {
    copy(titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold))
}

/** The gallery theme: follows the system unless the caller decides. */
@Composable
fun SeedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SeedTypography,
        content = content,
    )
}
