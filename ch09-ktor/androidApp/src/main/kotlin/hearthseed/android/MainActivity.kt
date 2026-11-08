// ch09-ktor/androidApp/src/main/kotlin/hearthseed/android/MainActivity.kt
package hearthseed.android

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import hearthseed.network.ApiConfig
import hearthseed.network.ApiResult
import hearthseed.network.HearthseedApi
import hearthseed.network.createHttpClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : Activity() {
    private val scope = MainScope()

    // 10.0.2.2 is the emulator's alias for the development machine's 127.0.0.1.
    private val client =
        createHttpClient(ApiConfig(baseUrl = "http://10.0.2.2:8085/api/v1/"))
    private val api = HearthseedApi(client, memberToken = "demo-token-android")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = TextView(this).apply { text = "Loading…" }
        setContentView(text)
        scope.launch {
            text.text = when (val result = api.varieties(pageSize = 10)) {
                is ApiResult.Success ->
                    result.value.items.joinToString("\n") { it.commonName }
                is ApiResult.Failure -> "Error: ${result.error}"
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        client.close()
        super.onDestroy()
    }
}
