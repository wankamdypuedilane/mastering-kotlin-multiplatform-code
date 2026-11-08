// mendfield/core/ui/src/commonMain/kotlin/mendfield/core/ui/Theme.kt
package mendfield.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Workshop = Color(0xFF2F5D50)  // green of the café's sign
private val Brass = Color(0xFFB0812A)

@Composable
fun MendfieldTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (dark) {
        darkColorScheme(primary = Color(0xFF8FCFBC), secondary = Brass)
    } else {
        lightColorScheme(primary = Workshop, secondary = Brass)
    }
    MaterialTheme(colorScheme = colors, content = content)
}
