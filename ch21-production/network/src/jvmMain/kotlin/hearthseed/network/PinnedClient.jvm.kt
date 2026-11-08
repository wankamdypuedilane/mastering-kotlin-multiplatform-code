// ch21-production/network/src/jvmMain/kotlin/hearthseed/network/PinnedClient.jvm.kt
package hearthseed.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CertificatePinner

actual fun pinnedHttpClient(pins: PinSet): HttpClient = HttpClient(OkHttp) {
    engine {
        config { // OkHttpClient.Builder
            val pinner = CertificatePinner.Builder()
                .add(pins.host, *pins.pins.toTypedArray())
                .build()
            certificatePinner(pinner)
        }
    }
}
