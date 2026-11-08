// ch21-production/network/src/iosMain/kotlin/hearthseed/network/PinnedClient.ios.kt
package hearthseed.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.certificates.CertificatePinner

actual fun pinnedHttpClient(pins: PinSet): HttpClient = HttpClient(Darwin) {
    engine {
        val pinner = CertificatePinner.Builder()
            .add(pins.host, *pins.pins.toTypedArray())
            .build()
        handleChallenge(pinner) // answers the server-trust challenge
    }
}
