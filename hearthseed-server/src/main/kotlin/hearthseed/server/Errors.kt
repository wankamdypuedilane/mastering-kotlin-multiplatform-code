// hearthseed-server/src/main/kotlin/hearthseed/server/Errors.kt
package hearthseed.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

/** An error with an HTTP status; StatusPages turns it into an [ErrorDto]. */
open class ApiException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String,
) : RuntimeException(message)

class NotFound(what: String) :
    ApiException(HttpStatusCode.NotFound, "not_found", "$what not found")

class Conflict(message: String) :
    ApiException(HttpStatusCode.Conflict, "lot_unavailable", message)

class Unauthorized :
    ApiException(HttpStatusCode.Unauthorized, "unauthorized",
        "Missing or invalid X-Member-Token header")

class InvalidInput(message: String) :
    ApiException(HttpStatusCode.BadRequest, "bad_request", message)

fun Application.installErrorHandling() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorDto(cause.code, cause.message))
        }
        // Malformed JSON bodies and unconvertible parameters.
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest,
                ErrorDto("bad_request", cause.message ?: "Bad request"))
        }
        // Unknown routes get the same error shape as everything else.
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ErrorDto("not_found", "No such endpoint"))
        }
    }
}
