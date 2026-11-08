// ch21-production/network/src/commonMain/kotlin/hearthseed/network/PinSet.kt
package hearthseed.network

import io.ktor.client.HttpClient

/**
 * Public-key pins for one host: base64 SHA-256 of each certificate's
 * SubjectPublicKeyInfo. A backup pin is mandatory, or a key rotation on the
 * server locks every installed app out until users update it.
 */
data class PinSet(val host: String, val pins: List<String>) {
    init {
        require(pins.size >= 2) { "Pin at least one backup key for $host" }
        require(pins.all { it.startsWith("sha256/") }) { "Use sha256/ pins" }
        require(pins.toSet().size == pins.size) { "Duplicate pins for $host" }
    }
}

/** Fictional placeholders: compute real pins from your own server's keys. */
val larkmoorPins = PinSet(
    host = "api.larkmoor.example",
    pins = listOf("sha256/YOUR_PRIMARY_PIN=", "sha256/YOUR_BACKUP_PIN="),
)

/** An HttpClient that refuses servers whose key matches none of [pins]. */
expect fun pinnedHttpClient(pins: PinSet): HttpClient
