// ch21-production/androidApp/src/main/kotlin/hearthseed/production/MainActivity.kt
package hearthseed.production

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import hearthseed.config.AppConfig
import hearthseed.secure.KeystoreSecureStore
import hearthseed.secure.SessionStore
import hearthseed.secure.appLogger
import hearthseed.secure.canRememberSession
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {
    private val scope = MainScope()
    private val log = appLogger(release = AppConfig.ENVIRONMENT == "production")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val label = TextView(this)
        setContentView(label)
        val sessions = SessionStore(KeystoreSecureStore(applicationContext))
        scope.launch {
            val session = sessions.load() // Keystore work runs on Dispatchers.IO
            log.i { "Session restored: $session" } // toString hides the token
            val state = if (session != null) "signed in" else "signed out"
            label.text =
                "${AppConfig.ENVIRONMENT}: $state, remember=$canRememberSession"
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
