// mendfield/core/sync/src/commonTest/kotlin/mendfield/core/sync/TransportTest.kt
package mendfield.core.sync

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.content.TextContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import mendfield.core.network.ApiConfig
import mendfield.core.network.MendfieldApi
import mendfield.core.network.createHttpClient

class TransportTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private fun transport(engine: MockEngine) = ApiSyncTransport(
        MendfieldApi(createHttpClient(ApiConfig(maxRetries = 0), engine), "demo-organizer-t"),
    )

    @Test fun mapsTheServerResults() = runTest {
        val engine = MockEngine {
            respond("""{"results":[{"operationId":"a","status":"APPLIED"},
                {"operationId":"b","status":"REJECTED","code":"visitor_busy",
                "message":"Ada is at another table"}]}""", headers = json)
        }
        val result = transport(engine).push(listOf(pending("a"), pending("b")))
        assertEquals(
            PushResult.Delivered(listOf(
                OperationResult.Applied("a"),
                OperationResult.Rejected("b", "visitor_busy", "Ada is at another table"),
            )),
            result,
        )
        val body = assertIs<TextContent>(engine.requestHistory.single().body).text
        assertEquals("/api/v1/sync", engine.requestHistory.single().url.encodedPath)
        assertEquals(true, body.startsWith("""{"operations":[{"type":"assign_slot","id":"a""""))
    }

    @Test fun aNetworkErrorIsTransient() = runTest {
        val engine = MockEngine { throw IOException("No route to host") }
        assertIs<PushResult.Transient>(transport(engine).push(listOf(pending("a"))))
    }

    @Test fun aServerErrorIsTransient() = runTest {
        val engine = MockEngine { respond("", HttpStatusCode.ServiceUnavailable) }
        assertIs<PushResult.Transient>(transport(engine).push(listOf(pending("a"))))
    }

    @Test fun aBadTokenIsPermanent() = runTest {
        val engine = MockEngine {
            respond("""{"code":"unauthorized","message":"Bad token"}""",
                HttpStatusCode.Unauthorized, json)
        }
        assertIs<PushResult.Permanent>(transport(engine).push(listOf(pending("a"))))
    }

    @Test fun everyOperationSurvivesTheTextCodec() {
        val ops = listOf(
            pending("1", Operation.RegisterItem(item("i-1"))),
            pending("2", Operation.AssignSlot("evt-02.fx-01.3", "i-1")),
            pending("3", Operation.LogOutcome(outcome("o-1"))),
        )
        ops.forEach { op ->
            assertEquals(op, OperationCodec.decode(OperationCodec.encode(op), op.createdAt))
        }
        assertEquals(
            """{"type":"assign_slot","id":"2","slotId":"evt-02.fx-01.3","itemId":"i-1"}""",
            OperationCodec.encode(ops[1]),
        )
    }
}
