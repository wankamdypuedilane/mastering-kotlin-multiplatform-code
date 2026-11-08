// mendfield/server/src/main/kotlin/mendfield/server/Routes.kt
package mendfield.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

const val ORGANIZER_TOKEN_HEADER = "X-Organizer-Token"
const val MAX_BATCH = 50

fun Route.mendfieldRoutes(store: RepairCafeStore) {
    get("/health") { call.respond(HealthDto("ok", store.eventCount)) }

    route("/api/v1") {
        install(OrganizerAuth) // items hold visitors' names: token or 401
        get("/events") { call.respond(store.events().map { it.toDto() }) }
        route("/events/{id}") {
            get { call.respond(store.event(call.id).toDto()) }
            get("/slots") { call.respond(store.slotsOf(call.id).toDtos()) }
            get("/items") { call.respond(store.itemsOf(call.id).toDtos()) }
            get("/outcomes") { call.respond(store.outcomesOf(call.id).toDtos()) }
            get("/stats") {
                call.respond(store.statsOf(call.id).toDto(call.id))
            }
        }
        get("/fixers") { call.respond(store.fixers().map { it.toDto() }) }

        post("/items") {
            val (item, created) = store.registerItem(call.receive<NewItemDto>())
            call.respond(created.status, item.toDto())
        }
        post("/slots/{id}/assign") {
            val itemId = call.receive<AssignRequest>().itemId
            call.respond(store.assign(call.id, itemId).toDto()) // or 404, 409
        }
        post("/outcomes") {
            val new = call.receive<NewOutcomeDto>()
            val (outcome, created) = store.logOutcome(new)
            call.respond(created.status, outcome.toDto())
        }
        post("/sync") {
            val operations = call.receive<SyncRequest>().operations
            if (operations.size !in 1..MAX_BATCH) {
                throw InvalidInput("A batch has 1 to $MAX_BATCH operations")
            }
            call.respond(SyncResponse(store.sync(operations)))
        }
    }
}

/** Every route below the one that installs it needs the organizer token. */
val OrganizerAuth = createRouteScopedPlugin("OrganizerAuth") {
    onCall { call -> call.requireOrganizerToken() }
}

/** The demo API accepts any token that starts with "demo-organizer-". */
private fun ApplicationCall.requireOrganizerToken() {
    val token = request.headers[ORGANIZER_TOKEN_HEADER].orEmpty()
    val prefix = "demo-organizer-"
    if (!token.startsWith(prefix) || token.length == prefix.length) {
        throw Unauthorized()
    }
}

private val ApplicationCall.id: String get() = parameters["id"]!!

/** 201 for something new, 200 for a repeat of an earlier request. */
private val Boolean.status: HttpStatusCode
    get() = if (this) HttpStatusCode.Created else HttpStatusCode.OK
