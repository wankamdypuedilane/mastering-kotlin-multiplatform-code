// mendfield/server/src/test/kotlin/mendfield/server/ApiTest.kt
package mendfield.server

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class ApiTest {
    private val now = Instant.parse("2026-11-08T10:00:00Z")
    private val fixedClock = object : Clock {
        override fun now(): Instant = now
    }

    private fun api(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) =
        testApplication {
            application { mendfieldModule(RepairCafeStore.fromResources(fixedClock)) }
            val client = createClient { install(ContentNegotiation) { json() } }
            block(client)
        }

    private fun HttpRequestBuilder.organizer() =
        header(ORGANIZER_TOKEN_HEADER, "demo-organizer-ada")

    private suspend inline fun <reified T> HttpClient.postJson(path: String, body: T) =
        post(path) {
            organizer()
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    private suspend fun HttpClient.sync(vararg ops: SyncOperationDto): HttpResponse =
        postJson("/api/v1/sync", SyncRequest(ops.toList()))

    @Test fun healthIsOpen() = api { client ->
        assertEquals(HealthDto("ok", 3), client.get("/health").body())
    }

    @Test fun everyApiRouteNeedsTheOrganizerToken() = api { client ->
        val none = client.get("/api/v1/events")
        assertEquals(HttpStatusCode.Unauthorized, none.status)
        assertEquals("unauthorized", none.body<ErrorDto>().code)
        for (token in listOf("YOUR_ORGANIZER_TOKEN", "demo-organizer-")) {
            val wrong = client.get("/api/v1/fixers") { header(ORGANIZER_TOKEN_HEADER, token) }
            assertEquals(HttpStatusCode.Unauthorized, wrong.status, token)
        }
        val post = client.post("/api/v1/sync") {
            contentType(ContentType.Application.Json)
            setBody("""{"operations":[]}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, post.status)
    }

    @Test fun eventsComeInDateOrder() = api { client ->
        val events: List<EventDto> = client.get("/api/v1/events") { organizer() }.body()
        assertEquals(listOf("evt-01", "evt-02", "evt-03"), events.map { it.id })
        assertEquals(
            EventDto("evt-02", "November Repair Café", "Brindle Street Community Hall",
                "2026-11-21", "10:00", "13:00", "Europe/London"),
            events[1],
        )
    }

    @Test fun anUnknownEventIs404() = api { client ->
        val response = client.get("/api/v1/events/evt-99/slots") { organizer() }
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(ErrorDto("not_found", "Event evt-99 not found"), response.body())
    }

    @Test fun slotsFollowTheEventTimeZone() = api { client ->
        val october: List<RepairSlotDto> =
            client.get("/api/v1/events/evt-01/slots") { organizer() }.body()
        val november: List<RepairSlotDto> =
            client.get("/api/v1/events/evt-02/slots") { organizer() }.body()
        val winter: List<RepairSlotDto> =
            client.get("/api/v1/events/evt-03/slots") { organizer() }.body()
        assertEquals(listOf(30, 36, 12), listOf(october.size, november.size, winter.size))
        assertEquals("2026-10-17T09:00:00Z", october.first().start) // 10:00 BST
        assertEquals("2026-11-21T10:00:00Z", november.first().start) // 10:00 GMT
        assertEquals("2026-12-09T18:45:00Z", winter.first().end) // 45-minute slots
        assertEquals("item-011", november.first { it.id == "evt-02.fx-01.1" }.itemId)
    }

    @Test fun nullsAreExplicitInJson() = api { client ->
        val text = client.get("/api/v1/events/evt-03/slots") { organizer() }.bodyAsText()
        assertTrue("\"itemId\":null" in text, text)
    }

    @Test fun statsUseTheSharedRules() = api { client ->
        val stats: StatsDto = client.get("/api/v1/events/evt-01/stats") { organizer() }.body()
        assertEquals(
            StatsDto("evt-01", registered = 10, withOutcome = 10,
                counts = mapOf("FIXED" to 5, "PARTIAL" to 2, "NOT_FIXED" to 2, "ADVICE" to 1),
                repairRatePercent = 78),
            stats,
        )
    }

    @Test fun itemsOutcomesAndFixers() = api { client ->
        val items: List<ItemDto> = client.get("/api/v1/events/evt-02/items") { organizer() }.body()
        assertEquals(listOf("item-011", "item-012", "item-013", "item-014"), items.map { it.id })
        val outcomes: List<OutcomeDto> =
            client.get("/api/v1/events/evt-01/outcomes") { organizer() }.body()
        assertEquals(10, outcomes.size)
        val fixers: List<FixerDto> = client.get("/api/v1/fixers") { organizer() }.body()
        assertEquals(8, fixers.size)
        assertEquals(listOf("ELECTRICAL", "ELECTRONICS"), fixers.first().skills)
    }

    @Test fun registeringAnItemGivesItAnIdAndATime() = api { client ->
        val response = client.postJson("/api/v1/items",
            NewItemDto("evt-02", "TOY", "  Teddy bear lost an eye ", "Ada Mensah"))
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(
            ItemDto("item-015", "evt-02", "TOY", "Teddy bear lost an eye", "Ada Mensah",
                "2026-11-08T10:00:00Z"),
            response.body(),
        )
    }

    @Test fun aClientIdMakesRegistrationIdempotent() = api { client ->
        val new = NewItemDto("evt-02", "LAMP", "Lamp hums", "Ada", id = "c0ffee-01")
        assertEquals(HttpStatusCode.Created, client.postJson("/api/v1/items", new).status)
        assertEquals(HttpStatusCode.OK, client.postJson("/api/v1/items", new).status)
        val clash = client.postJson("/api/v1/items", new.copy(description = "Other"))
        assertEquals(HttpStatusCode.Conflict, clash.status)
        assertEquals("id_conflict", clash.body<ErrorDto>().code)
    }

    @Test fun invalidItemsAreRefused() = api { client ->
        val cases = mapOf(
            NewItemDto("evt-02", "LAMP", "   ", "Ada") to HttpStatusCode.BadRequest,
            NewItemDto("evt-02", "SPACESHIP", "d", "Ada") to HttpStatusCode.BadRequest,
            NewItemDto("evt-02", "LAMP", "d", "Ada", id = "a/b") to HttpStatusCode.BadRequest,
            NewItemDto("evt-99", "LAMP", "d", "Ada") to HttpStatusCode.NotFound,
        )
        for ((body, status) in cases) {
            assertEquals(status, client.postJson("/api/v1/items", body).status, "$body")
        }
        val malformed = client.post("/api/v1/items") {
            organizer()
            contentType(ContentType.Application.Json)
            setBody("""{"eventId": 3}""")
        }
        assertEquals(HttpStatusCode.BadRequest, malformed.status)
    }

    @Test fun assigningFollowsTheSlotRules() = api { client ->
        suspend fun assign(slot: String, item: String) =
            client.postJson("/api/v1/slots/$slot/assign", AssignRequest(item))

        val ok = assign("evt-02.fx-07.1", "item-014")
        assertEquals(HttpStatusCode.OK, ok.status)
        assertEquals("item-014", ok.body<RepairSlotDto>().itemId)
        assertEquals(HttpStatusCode.OK, assign("evt-02.fx-07.1", "item-014").status) // again

        val cases = listOf(
            Triple("evt-02.fx-07.1", "item-013", "slot_taken"),
            Triple("evt-02.fx-08.2", "item-014", "item_scheduled"),
            // Gwen's lamp is at Maud's table 10:00-10:30, so her backpack cannot be.
            Triple("evt-02.fx-03.1", "item-013", "visitor_busy"),
            Triple("evt-01.fx-03.6", "item-013", "wrong_event"),
        )
        for ((slot, item, code) in cases) {
            val response = assign(slot, item)
            assertEquals(HttpStatusCode.Conflict, response.status, code)
            assertEquals(code, response.body<ErrorDto>().code)
        }
        assertEquals(HttpStatusCode.OK, assign("evt-02.fx-03.2", "item-013").status)
        assertEquals(HttpStatusCode.NotFound, assign("evt-02.fx-99.1", "item-013").status)
    }

    @Test fun loggingAnOutcome() = api { client ->
        val response = client.postJson("/api/v1/outcomes",
            NewOutcomeDto("item-011", "FIXED", "New switch"))
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(OutcomeDto("out-011", "item-011", "FIXED", "New switch",
            "2026-11-08T10:00:00Z"), response.body())
        assertEquals(HttpStatusCode.BadRequest,
            client.postJson("/api/v1/outcomes", NewOutcomeDto("item-011", "MAYBE")).status)
        assertEquals(HttpStatusCode.NotFound,
            client.postJson("/api/v1/outcomes", NewOutcomeDto("item-999", "FIXED")).status)
    }

    private val offlineItem = ItemDto("9b2e-item", "evt-02", "AUDIO", "Speaker crackles",
        "Kofi Asante", "2026-11-21T10:41:00Z")
    private val batch = arrayOf(
        RegisterItemOp("op-1", offlineItem),
        AssignSlotOp("op-2", slotId = "evt-02.fx-01.4", itemId = "9b2e-item"),
        AssignSlotOp("op-3", slotId = "evt-02.fx-01.1", itemId = "9b2e-item"), // taken
        LogOutcomeOp("op-4", OutcomeDto("9b2e-out", "9b2e-item", "FIXED",
            "Resoldered a wire", "2026-11-21T11:20:00Z")),
    )

    @Test fun syncAppliesABatchInOrder() = api { client ->
        val response = client.sync(*batch)
        assertEquals(HttpStatusCode.OK, response.status)
        val results = response.body<SyncResponse>().results
        assertEquals(
            listOf(SyncStatus.APPLIED, SyncStatus.APPLIED, SyncStatus.REJECTED,
                SyncStatus.APPLIED),
            results.map { it.status },
        )
        assertEquals("slot_taken", results[2].code)
        val items: List<ItemDto> = client.get("/api/v1/events/evt-02/items") { organizer() }.body()
        assertEquals(offlineItem, items.last()) // the client's id and time are kept
    }

    @Test fun aReplayGetsTheSameAnswerAndChangesNothing() = api { client ->
        val first = client.sync(*batch).bodyAsText()
        val replay = client.sync(*batch).bodyAsText()
        assertEquals(first, replay)
        val outcomes: List<OutcomeDto> =
            client.get("/api/v1/events/evt-02/outcomes") { organizer() }.body()
        assertEquals(1, outcomes.count { it.id == "9b2e-out" })
    }

    @Test fun reusingAnOperationIdForAnotherChangeIsRefused() = api { client ->
        client.sync(*batch)
        val reused = client.sync(AssignSlotOp("op-2", "evt-02.fx-01.5", "item-014"))
        val result = reused.body<SyncResponse>().results.single()
        assertEquals(SyncStatus.REJECTED to "operation_id_reused", result.status to result.code)
    }

    @Test fun aBatchMustHaveOneToFiftyOperations() = api { client ->
        assertEquals(HttpStatusCode.BadRequest, client.sync().status)
        val tooMany = (1..51).map { AssignSlotOp("op-$it", "evt-02.fx-01.2", "item-013") }
        assertEquals(HttpStatusCode.BadRequest, client.sync(*tooMany.toTypedArray()).status)
        val unknownType = client.post("/api/v1/sync") {
            organizer()
            contentType(ContentType.Application.Json)
            setBody("""{"operations":[{"type":"delete_everything","id":"x"}]}""")
        }
        assertEquals(HttpStatusCode.BadRequest, unknownType.status)
    }

    @Test fun anUnknownRouteIs404WithErrorDto() = api { client ->
        val response = client.get("/api/v2/nothing")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("not_found", response.body<ErrorDto>().code)
    }
}
