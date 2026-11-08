// mendfield/core/network/src/commonMain/kotlin/mendfield/core/network/MendfieldApi.kt
package mendfield.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import mendfield.core.model.Event
import mendfield.core.model.Fixer
import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.Outcome
import mendfield.core.model.OutcomeKind
import mendfield.core.model.RepairSlot

/** The Mendfield API, one suspend function per endpoint, models in and out. */
class MendfieldApi(
    private val client: HttpClient,
    private val organizerToken: String,
) {
    suspend fun health(): ApiResult<HealthDto> = apiCall {
        client.get("/health").body() // leading "/": outside /api/v1/
    }

    suspend fun events(): ApiResult<List<Event>> = apiCall {
        client.get { api("events") }.body<List<EventDto>>().map { it.toModel() }
    }

    suspend fun event(id: String): ApiResult<Event> = apiCall {
        client.get { api("events", id) }.body<EventDto>().toModel()
    }

    suspend fun slots(eventId: String): ApiResult<List<RepairSlot>> = apiCall {
        client.get { api("events", eventId, "slots") }
            .body<List<RepairSlotDto>>().map { it.toModel() }
    }

    suspend fun items(eventId: String): ApiResult<List<Item>> = apiCall {
        client.get { api("events", eventId, "items") }
            .body<List<ItemDto>>().map { it.toModel() }
    }

    suspend fun outcomes(eventId: String): ApiResult<List<Outcome>> = apiCall {
        client.get { api("events", eventId, "outcomes") }
            .body<List<OutcomeDto>>().map { it.toModel() }
    }

    suspend fun stats(eventId: String): ApiResult<StatsDto> = apiCall {
        client.get { api("events", eventId, "stats") }.body()
    }

    suspend fun fixers(): ApiResult<List<Fixer>> = apiCall {
        client.get { api("fixers") }.body<List<FixerDto>>().map { it.toModel() }
    }

    suspend fun registerItem(
        eventId: String,
        category: ItemCategory,
        description: String,
        visitorName: String,
        id: String? = null,
    ): ApiResult<Item> = apiCall {
        client.post {
            api("items")
            json(NewItemDto(eventId, category.name, description, visitorName,
                id))
        }.body<ItemDto>().toModel()
    }

    suspend fun assignSlot(
        slotId: String,
        itemId: String,
    ): ApiResult<RepairSlot> = apiCall {
        client.post {
            api("slots", slotId, "assign")
            json(AssignRequest(itemId))
        }.body<RepairSlotDto>().toModel()
    }

    suspend fun logOutcome(
        itemId: String,
        kind: OutcomeKind,
        note: String = "",
        id: String? = null,
    ): ApiResult<Outcome> = apiCall {
        client.post {
            api("outcomes")
            json(NewOutcomeDto(itemId, kind.name, note, id))
        }.body<OutcomeDto>().toModel()
    }

    /** Pushes outbox operations; one result per operation, in order. */
    suspend fun sync(
        operations: List<SyncOperationDto>,
    ): ApiResult<List<SyncResultDto>> = apiCall {
        client.post {
            api("sync")
            json(SyncRequest(operations))
        }.body<SyncResponse>().results
    }

    // Every /api/v1 route needs the organizer token. encodeSlash = true:
    // an id such as "a/../b" stays one path segment.
    private fun HttpRequestBuilder.api(vararg segments: String) {
        url { appendPathSegments(*segments, encodeSlash = true) }
        header(ORGANIZER_TOKEN_HEADER, organizerToken)
    }

    private inline fun <reified T : Any> HttpRequestBuilder.json(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
}
