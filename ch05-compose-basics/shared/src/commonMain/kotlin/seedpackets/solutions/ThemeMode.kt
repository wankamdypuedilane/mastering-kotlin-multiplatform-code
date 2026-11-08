package seedpackets.solutions

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import seedpackets.resources.Res
import seedpackets.resources.theme_dark
import seedpackets.resources.theme_light
import seedpackets.resources.theme_system
import seedpackets.ui.theme.SeedTheme

/** Exercise 5.3: three theme modes instead of a dark/light switch. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Saves a mode as its name: a String fits in every platform's saved state. */
val ThemeModeSaver: Saver<ThemeMode, String> = Saver(
    save = { it.name },
    restore = { ThemeMode.valueOf(it) },
)

@Composable
fun ThemeModeSelector(
    mode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = listOf(
        Res.string.theme_system, Res.string.theme_light, Res.string.theme_dark,
    )
    SingleChoiceSegmentedButtonRow(modifier) {
        ThemeMode.entries.forEachIndexed { index, entry ->
            SegmentedButton(
                selected = mode == entry,
                onClick = { onModeChange(entry) },
                shape = SegmentedButtonDefaults.itemShape(
                    index, ThemeMode.entries.size,
                ),
            ) { Text(stringResource(labels[index])) }
        }
    }
}

/** Owns the mode and resolves it; [content] is drawn in the chosen theme. */
@Composable
fun ThemeModeHost(content: @Composable () -> Unit) {
    var mode by rememberSaveable(stateSaver = ThemeModeSaver) {
        mutableStateOf(ThemeMode.SYSTEM)
    }
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    SeedTheme(darkTheme = dark) {
        Surface {
            Column {
                ThemeModeSelector(mode, onModeChange = { mode = it })
                content()
            }
        }
    }
}
