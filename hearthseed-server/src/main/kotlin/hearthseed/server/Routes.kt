// hearthseed-server/src/main/kotlin/hearthseed/server/Routes.kt
package hearthseed.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

const val MEMBER_TOKEN_HEADER = "X-Member-Token"
const val MAX_PAGE_SIZE = 50

fun Route.hearthseedRoutes(store: LibraryStore) {
    get("/health") { call.respond(HealthDto("ok", store.varietyCount)) }

    route("/api/v1") {
        get("/varieties") {
            val q = call.request.queryParameters
            val page = q.intOrDefault("page", 1)
            val pageSize = q.intOrDefault("pageSize", 20)
            if (page < 1) throw InvalidInput("page must be >= 1")
            if (pageSize !in 1..MAX_PAGE_SIZE) {
                throw InvalidInput("pageSize must be in 1..$MAX_PAGE_SIZE")
            }
            call.respond(store.searchVarieties(q["query"], q["family"], page, pageSize))
        }
        get("/varieties/{id}") {
            call.respond(store.variety(call.parameters["id"]!!)) // or 404
        }
        get("/varieties/{id}/lots") {
            call.respond(store.lotsOf(call.parameters["id"]!!))
        }
        post("/loans") {
            call.requireMemberToken()                       // or 401
            val request = call.receive<LoanRequest>()       // or 400
            call.respond(HttpStatusCode.Created, store.borrow(request)) // or 409
        }
        get("/members/{id}/loans") {
            call.requireMemberToken()
            call.respond(store.loansOf(call.parameters["id"]!!))
        }
    }
}

/** The demo API accepts any token that starts with "demo-token-". */
private fun ApplicationCall.requireMemberToken() {
    val token = request.headers[MEMBER_TOKEN_HEADER]
    if (token == null || !token.startsWith("demo-token-") ||
        token.length == "demo-token-".length
    ) {
        throw Unauthorized()
    }
}

private fun io.ktor.http.Parameters.intOrDefault(name: String, default: Int): Int {
    val raw = this[name] ?: return default
    return raw.toIntOrNull() ?: throw InvalidInput("$name must be an integer")
}
