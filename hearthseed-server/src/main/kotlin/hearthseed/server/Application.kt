// hearthseed-server/src/main/kotlin/hearthseed/server/Application.kt
package hearthseed.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

const val PORT = 8085

fun main() {
    // Loopback only: the demo token check is not real security.
    embeddedServer(Netty, port = PORT, host = "127.0.0.1") {
        hearthseedModule()
    }.start(wait = true)
}

fun Application.hearthseedModule(store: LibraryStore = LibraryStore.fromResources()) {
    install(ContentNegotiation) {
        json(Json { encodeDefaults = true; explicitNulls = true })
    }
    install(CallLogging)
    installErrorHandling()
    routing { hearthseedRoutes(store) }
}
