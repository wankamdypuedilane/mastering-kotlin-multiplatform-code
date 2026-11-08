package seedpackets.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import seedpackets.model.SampleCatalog
import seedpackets.resources.Res
import seedpackets.resources.app_title
import seedpackets.resources.dark_theme
import seedpackets.ui.theme.SeedTheme

/** The root composable shared by Android, iOS and desktop. */
@Composable
fun App() {
    val systemDark = isSystemInDarkTheme()
    // Survives configuration changes (Android) and process recreation.
    var darkTheme by rememberSaveable { mutableStateOf(systemDark) }

    SeedTheme(darkTheme = darkTheme) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.safeDrawingPadding()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(Res.string.app_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Text(stringResource(Res.string.dark_theme))
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = { darkTheme = it },
                        modifier = Modifier.testTag("dark-switch"),
                    )
                }
                SeedGallery(SampleCatalog.packets)
            }
        }
    }
}
