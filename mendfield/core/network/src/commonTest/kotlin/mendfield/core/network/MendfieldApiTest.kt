// mendfield/core/network/src/commonTest/kotlin/mendfield/core/network/MendfieldApiTest.kt
package mendfield.core.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.content.TextContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.io.IOException
import mendfield.core.model.ItemCategory
import mendfield.core.model.OutcomeKind
import mendfield.core.model.Skill

private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
private const val TOKEN = "demo-organizer-test"

// "UTC" works on every target; region ids need a time-zone database on web.
private const val EVENT = """{"id":"evt-02","title":"November Repair Café",
"venue":"Brindle Street Community Hall","date":"2026-11-21","opensAt":"10:00",
"closesAt":"13:00","timeZone":"UTC","addedLater":"unknown keys are ignored"}"""

private const val SLOT = """{"id":"evt-02.fx-01.1","eventId":"evt-02",
"fixerId":"fx-01","start":"2026-11-21T10:00:00Z","end":"2026-11-21T10:30:00Z",
"itemId":"item-011"}"""

private fun testApi(
    vararg handlers: MockRequestHandler,
    config: ApiConfig = ApiConfig(maxRetries = 0, retryBaseDelayMillis = 1),
): Pair<MendfieldApi, MockEngine> {
    val engine = MockEngine.Queue().apply { handlers.forEach(::enqueue) }
    return MendfieldApi(createHttpClient(config, engine), TOKEN) to engine
}

private fun MockEngine.bodyText(): String =
    assertIs<TextContent>(requestHistory.single().body).text

class MendfieldApiTest {
    @Test fun eventsAreMappedToModelsAndSendTheToken() = runTest {
        val (api, engine) = testApi({ respond("[$EVENT]", headers = jsonHeaders) })
        val event = assertIs<ApiResult.Success<List<mendfield.core.model.Event>>>(
            api.events()).value.single()
        assertEquals(LocalTime(10, 0), event.opensAt)
        assertEquals(TimeZone.UTC, event.timeZone)
        val request = engine.requestHistory.single()
        assertEquals("http://127.0.0.1:8086/api/v1/events", request.url.toString())
        assertEquals(TOKEN, request.headers[ORGANIZER_TOKEN_HEADER])
    }

    @Test fun slotsCarryInstantsAndTheAssignedItem() = runTest {
        val (api, engine) = testApi({
            respond("[$SLOT,${SLOT.replace("\"item-011\"", "null")
                .replace(".1\"", ".2\"")}]", headers = jsonHeaders)
        })
        val slots = assertIs<ApiResult.Success<List<mendfield.core.model.RepairSlot>>>(
            api.slots("evt-02")).value
        assertEquals(Instant.parse("2026-11-21T10:00:00Z"), slots[0].start)
        assertEquals(listOf(false, true), slots.map { it.isOpen })
        assertEquals("/api/v1/events/evt-02/slots",
            engine.requestHistory.single().url.encodedPath)
    }

    @Test fun idsStayOnePathSegment() = runTest {
        val (api, engine) = testApi({ respond(EVENT, headers = jsonHeaders) })
        api.event("a/../b")
        assertEquals("/api/v1/events/a%2F..%2Fb",
            engine.requestHistory.single().url.encodedPath)
    }

    @Test fun unknownSkillsOfANewerServerAreSkipped() = runTest {
        val (api, _) = testApi({
            respond("""[{"id":"fx-01","displayName":"Maud Okafor",
                "skills":["ELECTRICAL","WELDING"]}]""", headers = jsonHeaders)
        })
        val fixer = assertIs<ApiResult.Success<List<mendfield.core.model.Fixer>>>(
            api.fixers()).value.single()
        assertEquals(setOf(Skill.ELECTRICAL), fixer.skills)
    }

    @Test fun anUnknownCategoryIsBadData() = runTest {
        val (api, _) = testApi({
            respond("""[{"id":"i","eventId":"e","category":"SPACESHIP",
                "description":"d","visitorName":"v",
                "registeredAt":"2026-11-21T10:00:00Z"}]""", headers = jsonHeaders)
        })
        val failure = assertIs<ApiResult.Failure>(api.items("e"))
        assertIs<ApiError.Unexpected>(failure.error)
    }

    @Test fun registerItemPostsJson() = runTest {
        val (api, engine) = testApi({
            respond("""{"id":"item-015","eventId":"evt-02","category":"LAMP",
                "description":"Flickers","visitorName":"Ada",
                "registeredAt":"2026-11-08T10:00:00Z"}""",
                HttpStatusCode.Created, jsonHeaders)
        })
        val item = assertIs<ApiResult.Success<mendfield.core.model.Item>>(
            api.registerItem("evt-02", ItemCategory.LAMP, "Flickers", "Ada")).value
        assertEquals("item-015", item.id)
        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals(
            """{"eventId":"evt-02","category":"LAMP","description":"Flickers",""" +
                """"visitorName":"Ada"}""",
            engine.bodyText(),
        )
    }

    @Test fun aTakenSlotIsAConflictWithItsCode() = runTest {
        val (api, _) = testApi({
            respond("""{"code":"slot_taken","message":"Slot s holds item-011"}""",
                HttpStatusCode.Conflict, jsonHeaders)
        })
        assertEquals(
            ApiResult.Failure(ApiError.Conflict("slot_taken", "Slot s holds item-011")),
            api.assignSlot("s", "item-012"),
        )
    }

    @Test fun logOutcomeSendsTheKindName() = runTest {
        val (api, engine) = testApi({
            respond("""{"id":"o-1","itemId":"item-011","kind":"PARTIAL",
                "note":"Needs a part","loggedAt":"2026-11-21T11:00:00Z"}""",
                HttpStatusCode.Created, jsonHeaders)
        })
        val outcome = assertIs<ApiResult.Success<mendfield.core.model.Outcome>>(
            api.logOutcome("item-011", OutcomeKind.PARTIAL, "Needs a part")).value
        assertEquals(OutcomeKind.PARTIAL, outcome.kind)
        assertEquals("""{"itemId":"item-011","kind":"PARTIAL","note":"Needs a part"}""",
            engine.bodyText())
    }

    @Test fun syncEncodesEachOperationWithItsType() = runTest {
        val (api, engine) = testApi({
            respond("""{"results":[{"operationId":"op-1","status":"APPLIED"},
                {"operationId":"op-2","status":"REJECTED","code":"slot_taken",
                "message":"taken"}]}""", headers = jsonHeaders)
        })
        val ops = listOf(
            AssignSlotOp("op-1", slotId = "s1", itemId = "i1"),
            LogOutcomeOp("op-2", OutcomeDto("o1", "i1", "FIXED", "", "2026-11-21T11:00:00Z")),
        )
        val results = assertIs<ApiResult.Success<List<SyncResultDto>>>(api.sync(ops)).value
        assertEquals(listOf(SyncStatus.APPLIED, SyncStatus.REJECTED), results.map { it.status })
        assertEquals("slot_taken", results[1].code)
        assertEquals(
            """{"operations":[{"type":"assign_slot","id":"op-1","slotId":"s1",""" +
                """"itemId":"i1"},{"type":"log_outcome","id":"op-2","outcome":""" +
                """{"id":"o1","itemId":"i1","kind":"FIXED","note":"",""" +
                """"loggedAt":"2026-11-21T11:00:00Z"}}]}""",
            engine.bodyText(),
        )
    }

    @Test fun mapsUnauthorized() = runTest {
        val (api, _) = testApi({
            respond("""{"code":"unauthorized","message":"Missing token"}""",
                HttpStatusCode.Unauthorized, jsonHeaders)
        })
        assertEquals(ApiResult.Failure(ApiError.Unauthorized("Missing token")), api.fixers())
    }

    @Test fun retriesServerErrorsOnGetOnly() = runTest {
        val config = ApiConfig(maxRetries = 1, retryBaseDelayMillis = 1)
        val (getApi, getEngine) = testApi(
            { respondError(HttpStatusCode.ServiceUnavailable) },
            { respond("[]", headers = jsonHeaders) },
            config = config,
        )
        assertEquals(ApiResult.Success(emptyList()), getApi.events())
        assertEquals(2, getEngine.requestHistory.size)

        val (postApi, postEngine) = testApi(
            { respondError(HttpStatusCode.ServiceUnavailable) }, config = config)
        val failure = assertIs<ApiResult.Failure>(postApi.sync(emptyList()))
        assertEquals(ApiError.Server(503), failure.error)
        assertTrue(failure.error.isTransient)
        assertEquals(1, postEngine.requestHistory.size)
    }

    @Test fun anIoFailureIsATransientNetworkError() = runTest {
        val (api, _) = testApi({ throw IOException("Connection refused") })
        val failure = assertIs<ApiResult.Failure>(api.events())
        assertEquals(ApiError.Network("Connection refused"), failure.error)
        assertTrue(failure.error.isTransient)
    }

    @Test fun logsHeadersButNeverTheToken() = runTest {
        val lines = mutableListOf<String>()
        val logger = object : Logger {
            override fun log(message: String) { lines += message }
        }
        val (api, _) = testApi(
            { respond("[]", headers = jsonHeaders) },
            config = ApiConfig(logLevel = LogLevel.HEADERS, logger = logger, maxRetries = 0),
        )
        api.fixers()
        val log = lines.joinToString("\n")
        assertTrue("$ORGANIZER_TOKEN_HEADER: ***" in log, log)
        assertFalse(TOKEN in log, log)
    }

    @Test fun rejectsABaseUrlWithoutTrailingSlash() {
        assertFailsWith<IllegalArgumentException> {
            ApiConfig(baseUrl = "http://127.0.0.1:8086/api/v1")
        }
    }
}
