// ch22-release/androidApp/src/main/kotlin/release/MainActivity.kt
package release

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AboutScreen() }
    }
}
