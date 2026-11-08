// mendfield/androidApp/src/main/kotlin/mendfield/android/MainActivity.kt
package mendfield.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mendfield.shared.App

/** The only activity: everything else is a destination of the NavHost. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // Scaffold, TopAppBar and the sync bar pad for bars
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
