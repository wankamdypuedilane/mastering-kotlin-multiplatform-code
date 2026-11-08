// mendfield/server/src/main/kotlin/mendfield/server/Application.kt
package mendfield.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

const val PORT = 8086

fun main() {
    // Loopback only: the demo token check is not real security.
    embeddedServer(Netty, port = PORT, host = "127.0.0.1") {
        mendfieldModule()
    }.start(wait = true)
}

fun Application.mendfieldModule(
    store: RepairCafeStore = RepairCafeStore.fromResources(),
) {
    install(ContentNegotiation) {
        json(Json { encodeDefaults = true; explicitNulls = true })
    }
    install(CallLogging)
    installErrorHandling()
    routing { mendfieldRoutes(store) }
}
