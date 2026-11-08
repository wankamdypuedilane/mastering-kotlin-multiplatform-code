// ch09-ktor/solutions/src/commonTest/kotlin/hearthseed/solutions/SolutionsTest.kt
package hearthseed.solutions

import hearthseed.network.ApiConfig
import hearthseed.network.ApiError
import hearthseed.network.ApiResult
import hearthseed.network.HearthseedApi
import hearthseed.network.LoanDto
import hearthseed.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.content.TextContent
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

private val json = headersOf(HttpHeaders.ContentType, "application/json")
private val config = ApiConfig(maxRetries = 0, retryBaseDelayMillis = 1)

private fun engineOf(vararg handlers: MockRequestHandler) =
    MockEngine.Queue().apply { handlers.forEach(::enqueue) }

private fun variety(id: String) = """{"id":"$id","commonName":"Name $id",
"latinName":"L","family":"Fabaceae","daysToMaturity":60,"sowingMonths":[4],
"difficulty":"EASY","description":"d"}"""

private fun page(ids: List<String>, page: Int, size: Int, total: Int) =
    """{"items":[${ids.joinToString(",") { variety(it) }}],
"page":$page,"pageSize":$size,"total":$total}"""

private fun lot(id: String, rate: Int, status: String) = """{"id":"$id",
"varietyId":"var-009","harvestYear":2025,"grams":20,"germinationRate":$rate,
"status":"$status"}"""

private fun loan(lotId: String) = """{"id":"loan-9","lotId":"$lotId",
"memberId":"mem-001","borrowedOn":"2026-11-08","dueSeason":2026}"""

class SolutionsTest {
    @Test fun ex1SendsTheClientHeaderAndKeepsTheBaseUrl() = runTest {
        val engine = engineOf({ respond("""{"status":"ok","varieties":1}""",
            headers = json) })
        val client = createHttpClient(config, engine).identifiedAs("hearthseed/1.0")
        HearthseedApi(client, "demo-token-x").health()
        val request = engine.requestHistory.single()
        assertEquals("hearthseed/1.0", request.headers[CLIENT_HEADER])
        assertEquals("http://127.0.0.1:8085/health", request.url.toString())
    }

    @Test fun ex2KeepsOnlyLoansNotReturned() = runTest {
        val engine = engineOf({
            respond("""[${loan("lot-001")},
                {"id":"loan-3","lotId":"lot-010","memberId":"mem-001",
                 "borrowedOn":"2025-04-19","dueSeason":2025,"returnedGrams":40}]""",
                headers = json)
        })
        val api = HearthseedApi(createHttpClient(config, engine), "demo-token-x")
        val open = assertIs<ApiResult.Success<List<*>>>(api.openLoans("mem-001"))
        assertEquals(1, open.value.size)
    }

    @Test fun ex2PassesFailuresThrough() = runTest {
        val engine = engineOf({ respondError(HttpStatusCode.Unauthorized) })
        val api = HearthseedApi(createHttpClient(config, engine), "bad")
        assertEquals(ApiResult.Failure(ApiError.Unauthorized("Unauthorized")),
            api.openLoans("mem-001"))
    }

    @Test fun ex3ReadsEveryPage() = runTest {
        val engine = engineOf(
            { respond(page(listOf("a", "b"), 1, 2, 5), headers = json) },
            { respond(page(listOf("c", "d"), 2, 2, 5), headers = json) },
            { respond(page(listOf("e"), 3, 2, 5), headers = json) },
        )
        val api = HearthseedApi(createHttpClient(config, engine), "demo-token-x")
        val all = assertIs<ApiResult.Success<List<*>>>(api.allVarieties(pageSize = 2))
        assertEquals(5, all.value.size)
        assertEquals(listOf("1", "2", "3"),
            engine.requestHistory.map { it.url.parameters["page"] })
    }

    @Test fun ex3StopsAtTheFirstError() = runTest {
        val engine = engineOf(
            { respond(page(listOf("a", "b"), 1, 2, 5), headers = json) },
            { respondError(HttpStatusCode.InternalServerError) },
        )
        val api = HearthseedApi(createHttpClient(config, engine), "demo-token-x")
        assertEquals(ApiResult.Failure(ApiError.Server(500)), api.allVarieties(pageSize = 2))
    }

    @Test fun ex4TriesTheNextLotAfterAConflict() = runTest {
        val engine = engineOf(
            { respond("""[${lot("lot-1", 70, "AVAILABLE")},
                ${lot("lot-2", 95, "AVAILABLE")},${lot("lot-3", 99, "ON_LOAN")}]""",
                headers = json) },
            { respondError(HttpStatusCode.Conflict) }, // lot-2 was just taken
            { respond(loan("lot-1"), HttpStatusCode.Created, json) },
        )
        val api = HearthseedApi(createHttpClient(config, engine), "demo-token-x")
        val loan = api.borrowAnyLot("var-009", "mem-001")
        assertEquals("lot-1", assertIs<ApiResult.Success<LoanDto>>(loan).value.lotId)
        // Best germination rate first: lot-2, then lot-1. lot-3 is on loan.
        val tried = engine.requestHistory.drop(1)
            .map { (it.body as TextContent).text.substringAfter("\"lotId\":\"").take(5) }
        assertEquals(listOf("lot-2", "lot-1"), tried)
    }

    @Test fun ex4ReportsConflictWhenNothingIsAvailable() = runTest {
        val engine = engineOf({ respond("""[${lot("lot-3", 99, "ON_LOAN")}]""",
            headers = json) })
        val api = HearthseedApi(createHttpClient(config, engine), "demo-token-x")
        assertEquals(ApiResult.Failure(ApiError.Conflict("No available lot of var-009")),
            api.borrowAnyLot("var-009", "mem-001"))
    }
}
