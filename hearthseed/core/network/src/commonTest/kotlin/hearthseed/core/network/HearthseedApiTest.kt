// hearthseed/core/network/src/commonTest/kotlin/hearthseed/core/network/HearthseedApiTest.kt
package hearthseed.core.network

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException

private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

private const val TOMATO = """{"id":"var-001","commonName":"Larkmoor Ember tomato",
"latinName":"Solanum lycopersicum","family":"Solanaceae","daysToMaturity":72,
"sowingMonths":[3,4],"difficulty":"MEDIUM","description":"Round red fruits.",
"addedLater":"unknown keys are ignored"}"""

/** A test API whose engine answers with [handlers], one per request. */
private fun testApi(
    vararg handlers: MockRequestHandler,
    config: ApiConfig = ApiConfig(maxRetries = 0, retryBaseDelayMillis = 1),
): Pair<HearthseedApi, MockEngine> {
    val engine = MockEngine.Queue().apply { handlers.forEach(::enqueue) }
    val client = createHttpClient(config, engine)
    return HearthseedApi(client, memberToken = "demo-token-test") to engine
}

class HearthseedApiTest {
    @Test fun buildsUrlWithQueryParametersAndDecodesPage() = runTest {
        val (api, engine) = testApi({
            respond("""{"items":[$TOMATO],"page":1,"pageSize":20,"total":1}""",
                headers = jsonHeaders)
        })
        val result = api.varieties(query = "tomato")
        val page = assertIs<ApiResult.Success<Page<VarietyDto>>>(result).value
        assertEquals("Larkmoor Ember tomato", page.items.single().commonName)
        assertEquals(
            "http://127.0.0.1:8085/api/v1/varieties?query=tomato&page=1&pageSize=20",
            engine.requestHistory.single().url.toString(),
        )
    }

    @Test fun encodesIdsAsOnePathSegment() = runTest {
        val (api, engine) = testApi({ respond(TOMATO, headers = jsonHeaders) })
        api.variety("a/../b")
        assertEquals("/api/v1/varieties/a%2F..%2Fb",
            engine.requestHistory.single().url.encodedPath)
    }

    @Test fun healthUsesTheRootPath() = runTest {
        val (api, engine) = testApi({
            respond("""{"status":"ok","varieties":40}""", headers = jsonHeaders)
        })
        assertEquals(ApiResult.Success(HealthDto("ok", 40)), api.health())
        assertEquals("/health", engine.requestHistory.single().url.encodedPath)
    }

    @Test fun mapsNotFoundWithErrorBody() = runTest {
        val (api, _) = testApi({
            respond("""{"code":"not_found","message":"Variety var-9 not found"}""",
                HttpStatusCode.NotFound, jsonHeaders)
        })
        assertEquals(ApiResult.Failure(ApiError.NotFound("Variety var-9 not found")),
            api.variety("var-9"))
    }

    @Test fun mapsErrorWithoutJsonBodyToStatusDescription() = runTest {
        val (api, _) = testApi({ respondError(HttpStatusCode.NotFound, "<html>") })
        assertEquals(ApiResult.Failure(ApiError.NotFound("Not Found")), api.lots("x"))
    }

    @Test fun borrowSendsTokenAndJsonAndMapsConflict() = runTest {
        val (api, engine) = testApi({
            respond("""{"code":"lot_unavailable","message":"Lot lot-001 is ON_LOAN"}""",
                HttpStatusCode.Conflict, jsonHeaders)
        })
        val result = api.borrow("lot-001", "mem-002")
        assertEquals(ApiResult.Failure(ApiError.Conflict("Lot lot-001 is ON_LOAN")), result)
        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("demo-token-test", request.headers[MEMBER_TOKEN_HEADER])
        val body = assertIs<TextContent>(request.body)
        assertEquals("""{"lotId":"lot-001","memberId":"mem-002"}""", body.text)
    }

    @Test fun mapsUnauthorized() = runTest {
        val (api, _) = testApi({
            respond("""{"code":"unauthorized","message":"Missing token"}""",
                HttpStatusCode.Unauthorized, jsonHeaders)
        })
        assertEquals(ApiResult.Failure(ApiError.Unauthorized("Missing token")),
            api.loansOf("mem-001"))
    }

    @Test fun mapsSlowServerToTimeout() = runTest {
        val (api, _) = testApi(
            { delay(5_000); respond(TOMATO, headers = jsonHeaders) },
            config = ApiConfig(requestTimeoutMillis = 100, maxRetries = 0),
        )
        assertEquals(ApiResult.Failure(ApiError.Timeout), api.variety("var-001"))
    }

    @Test fun mapsIoFailureToNetworkAfterRetries() = runTest {
        var attempts = 0
        val fail: MockRequestHandler = { attempts++; throw IOException("Connection refused") }
        val (api, _) = testApi(fail, fail, fail,
            config = ApiConfig(maxRetries = 2, retryBaseDelayMillis = 1))
        assertEquals(ApiResult.Failure(ApiError.Network("Connection refused")),
            api.varieties())
        assertEquals(3, attempts) // the first try and two retries
    }

    @Test fun retriesServerErrorsOnGet() = runTest {
        val (api, engine) = testApi(
            { respondError(HttpStatusCode.ServiceUnavailable) },
            { respondError(HttpStatusCode.BadGateway) },
            { respond(TOMATO, headers = jsonHeaders) },
            config = ApiConfig(maxRetries = 2, retryBaseDelayMillis = 1),
        )
        assertIs<ApiResult.Success<VarietyDto>>(api.variety("var-001"))
        assertEquals(3, engine.requestHistory.size)
    }

    @Test fun neverRetriesPost() = runTest {
        val (api, engine) = testApi(
            { respondError(HttpStatusCode.ServiceUnavailable) },
            config = ApiConfig(maxRetries = 2, retryBaseDelayMillis = 1),
        )
        assertEquals(ApiResult.Failure(ApiError.Server(503)), api.borrow("lot-002", "m"))
        assertEquals(1, engine.requestHistory.size)
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
        api.loansOf("mem-001")
        val log = lines.joinToString("\n")
        assertTrue("$MEMBER_TOKEN_HEADER: ***" in log, log)
        assertFalse("demo-token-test" in log, log)
    }

    @Test fun rejectsBaseUrlWithoutTrailingSlash() {
        assertFailsWith<IllegalArgumentException> {
            ApiConfig(baseUrl = "http://127.0.0.1:8085/api/v1")
        }
    }
}
