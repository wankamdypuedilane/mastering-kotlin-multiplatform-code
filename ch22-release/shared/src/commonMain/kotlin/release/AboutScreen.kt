// ch22-release/shared/src/commonMain/kotlin/release/AboutScreen.kt
package release

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** The one screen every packaged build shows: which app and which version. */
@Composable
fun AboutScreen(version: String = APP_VERSION) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Hearthseed", style = MaterialTheme.typography.headlineMedium)
                Text("Larkmoor Commons Seed Library")
                Text("Version $version")
            }
        }
    }
}
