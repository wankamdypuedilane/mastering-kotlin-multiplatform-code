// ch09-ktor/client/src/commonTest/kotlin/hearthseed/network/BearerAuthTest.kt
package hearthseed.network

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.content.TextContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

private val json = headersOf(HttpHeaders.ContentType, "application/json")

private class MemoryTokenStore(var tokens: TokenPair?) : TokenStore {
    override suspend fun load() = tokens
    override suspend fun save(tokens: TokenPair) { this.tokens = tokens }
}

class BearerAuthTest {
    @Test fun refreshesTheTokensAfter401AndRetriesOnce() = runTest {
        val store = MemoryTokenStore(TokenPair("demo-access-1", "demo-refresh-1"))
        val engine = MockEngine { request ->
            val auth = request.headers[HttpHeaders.Authorization]
            when {
                request.url.encodedPath.endsWith("/auth/refresh") -> respond(
                    """{"accessToken":"demo-access-2","refreshToken":"demo-refresh-2"}""",
                    headers = json,
                )
                auth == "Bearer demo-access-2" -> respond(
                    """{"status":"ok","varieties":40}""", headers = json)
                else -> respond( // the access token has expired
                    """{"code":"unauthorized","message":"Token expired"}""",
                    HttpStatusCode.Unauthorized,
                    headersOf(HttpHeaders.WWWAuthenticate to listOf("Bearer"),
                        HttpHeaders.ContentType to listOf("application/json")),
                )
            }
        }
        val client = createHttpClient(ApiConfig(maxRetries = 0), engine)
            .config { installBearerAuth(store) }
        val api = HearthseedApi(client, memberToken = "demo-token-test")

        assertIs<ApiResult.Success<HealthDto>>(api.health())

        val history = engine.requestHistory
        assertEquals(listOf(HttpMethod.Get, HttpMethod.Post, HttpMethod.Get),
            history.map { it.method })
        assertEquals("Bearer demo-access-1", history[0].headers[HttpHeaders.Authorization])
        assertNull(history[1].headers[HttpHeaders.Authorization]) // refresh call
        assertEquals("""{"refreshToken":"demo-refresh-1"}""",
            (history[1].body as TextContent).text)
        assertEquals("Bearer demo-access-2", history[2].headers[HttpHeaders.Authorization])
        assertEquals(TokenPair("demo-access-2", "demo-refresh-2"), store.tokens)
    }

    @Test fun aFailedRefreshEndsAsUnauthorized() = runTest {
        val store = MemoryTokenStore(TokenPair("demo-access-1", "demo-refresh-1"))
        val engine = MockEngine { _ ->
            respond("""{"code":"unauthorized","message":"Signed out"}""",
                HttpStatusCode.Unauthorized,
                headersOf(HttpHeaders.WWWAuthenticate to listOf("Bearer"),
                    HttpHeaders.ContentType to listOf("application/json")))
        }
        val client = createHttpClient(ApiConfig(maxRetries = 0), engine)
            .config { installBearerAuth(store) }
        val api = HearthseedApi(client, memberToken = "demo-token-test")

        assertEquals(ApiResult.Failure(ApiError.Unauthorized("Signed out")), api.health())
        assertEquals(2, engine.requestHistory.size) // the call and one refresh
    }
}
