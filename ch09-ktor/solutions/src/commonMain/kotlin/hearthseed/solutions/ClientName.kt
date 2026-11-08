// ch09-ktor/solutions/src/commonMain/kotlin/hearthseed/solutions/ClientName.kt
package hearthseed.solutions

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header

const val CLIENT_HEADER = "X-Client"

/** Exercise 9.1: a copy of [client] that names itself on every request. */
fun HttpClient.identifiedAs(name: String): HttpClient = config {
    // Added to the existing defaultRequest block, which keeps the base URL.
    defaultRequest { header(CLIENT_HEADER, name) }
}
