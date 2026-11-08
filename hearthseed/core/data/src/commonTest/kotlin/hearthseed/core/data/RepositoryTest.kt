// hearthseed/core/data/src/commonTest/kotlin/hearthseed/core/data/RepositoryTest.kt
package hearthseed.core.data

import hearthseed.core.data.local.InMemoryVarietyLocalSource
import hearthseed.core.model.LotStatus
import hearthseed.core.network.ApiConfig
import hearthseed.core.network.HearthseedApi
import hearthseed.core.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException

private val json = headersOf(HttpHeaders.ContentType, "application/json")

private fun variety(
    id: String, name: String, days: Int = 60, difficulty: String = "EASY",
) = """{"id":"$id",
"commonName":"$name","latinName":"Pisum sativum","family":"Fabaceae",
"daysToMaturity":$days,"sowingMonths":[3,4],"difficulty":"$difficulty",
"description":"A pea."}"""

private const val LOTS = """[
{"id":"lot-1","varietyId":"var-1","harvestYear":2024,"grams":8,
 "germinationRate":70,"donorMemberId":null,"status":"AVAILABLE"},
{"id":"lot-2","varietyId":"var-1","harvestYear":2023,"grams":50,
 "germinationRate":88,"donorMemberId":"mem-4","status":"ON_LOAN"}]"""

/** A tiny router: the MockEngine answers like hearthseed-server would. */
private class TestServer(
    val routes: MutableMap<String, MockRequestHandleScope.(HttpRequestData) ->
        HttpResponseData> = mutableMapOf(),
) {
    val requests = mutableListOf<HttpRequestData>()
    val engine = MockEngine { request ->
        requests += request
        val key = "${request.method.value} ${request.url.encodedPath}"
        val route = routes[key] ?: error("no route for $key")
        route(request)
    }
    val api = HearthseedApi(
        createHttpClient(ApiConfig(maxRetries = 0), engine),
        memberToken = "demo-token-test",
    )
}

class RepositoryTest {
    private val server = TestServer()
    private val local = InMemoryVarietyLocalSource()
    private val catalog = DefaultCatalogRepository(server.api, local)
    private val loans = DefaultLoanRepository(server.api, local, catalog,
        MemberSession("mem-3", "Cleo", "demo-token-test"))

    @Test fun pageSendsTrimmedFiltersAndKeepsItemsLocally() = runTest {
        server.routes["GET /api/v1/varieties"] = {
            respond("""{"items":[${variety("var-1", "Larkmoor Pea")}],
                "page":2,"pageSize":1,"total":3}""", headers = json)
        }
        val page = catalog.varieties("  pea ", "Fabaceae", page = 2, pageSize = 1)
        val value = assertIs<DataResult.Success<VarietyPage>>(page).value
        assertEquals("Larkmoor Pea", value.items.single().commonName)
        assertTrue(value.hasMore) // 2 * 1 < 3
        assertEquals("query=pea&family=Fabaceae&page=2&pageSize=1",
            server.requests.single().url.encodedQuery)

        // The detail screen now gets the variety without a request.
        assertIs<DataResult.Success<*>>(catalog.variety("var-1"))
        assertEquals(1, server.requests.size)
    }

    @Test fun blankQueryIsLeftOutAndInvalidItemsAreDropped() = runTest {
        server.routes["GET /api/v1/varieties"] = {
            respond("""{"items":[${variety("var-1", "Pea")},
                ${variety("var-2", "Broken", days = 0)}],
                "page":1,"pageSize":20,"total":2}""", headers = json)
        }
        val page = catalog.varieties(" ", null, page = 1).getOrNull()!!
        assertEquals(listOf("var-1"), page.items.map { it.id })
        assertEquals("page=1&pageSize=20", server.requests.single().url.encodedQuery)
    }

    @Test fun unknownEnumValuesDropOneItemNotThePage() = runTest {
        server.routes["GET /api/v1/varieties"] = {
            respond("""{"items":[${variety("var-1", "Pea")},
                ${variety("var-2", "Odd", difficulty = "EXPERT")}],
                "page":1,"pageSize":20,"total":2}""", headers = json)
        }
        server.routes["GET /api/v1/varieties/var-2"] = {
            respond(variety("var-2", "Odd", difficulty = "EXPERT"), headers = json)
        }
        server.routes["GET /api/v1/varieties/var-1/lots"] = {
            respond(LOTS.replace("ON_LOAN", "IN_QUARANTINE"), headers = json)
        }
        val page = catalog.varieties("", null, page = 1).getOrNull()!!
        assertEquals(listOf("var-1"), page.items.map { it.id })
        assertIs<DataError.Unexpected>(
            assertIs<DataResult.Failure>(catalog.variety("var-2")).error)
        val lots = catalog.refreshLots("var-1").getOrNull()!!
        assertEquals(listOf("lot-1"), lots.map { it.id })
    }

    @Test fun offlineFallbackServesStoredVarietiesWhenTheServerIsGone() = runTest {
        val repository = OfflineFallbackCatalogRepository(catalog, local)
        var online = true
        server.routes["GET /api/v1/varieties"] = {
            if (!online) throw IOException("refused")
            respond("""{"items":[${variety("var-1", "Larkmoor Pea")},
                ${variety("var-2", "Quarry Bean")}],
                "page":1,"pageSize":20,"total":2}""", headers = json)
        }
        // Online: the network answers, and the page is stored locally.
        val fromServer = repository.varieties("", null, page = 1).getOrNull()!!
        assertEquals(listOf("var-1", "var-2"), fromServer.items.map { it.id })
        assertEquals(false, repository.servedFromDevice.value)
        assertEquals(1, server.requests.size)

        // Offline: the same call is answered from the local source.
        online = false
        val fromDevice = repository.varieties("pea", null, page = 1).getOrNull()!!
        assertEquals(listOf("var-1"), fromDevice.items.map { it.id })
        assertEquals(1, fromDevice.total)
        assertTrue(repository.servedFromDevice.value)
        assertEquals(2, server.requests.size) // it did try the network first
    }

    @Test fun offlineFallbackPassesOtherErrorsAndAnEmptyDeviceThrough() = runTest {
        val repository = OfflineFallbackCatalogRepository(catalog, local)
        server.routes["GET /api/v1/varieties"] = { throw IOException("refused") }
        assertEquals(DataResult.Failure(DataError.Offline),
            repository.varieties("", null, page = 1)) // nothing seen yet
        server.routes["GET /api/v1/varieties"] = {
            respond("""{"code":"oops","message":"down"}""",
                HttpStatusCode.ServiceUnavailable, json)
        }
        assertEquals(DataResult.Failure(DataError.Server(503)),
            repository.varieties("", null, page = 1))
    }

    @Test fun unknownVarietyIsNotFoundAndNoNetworkIsOffline() = runTest {
        server.routes["GET /api/v1/varieties/var-9"] = {
            respond("""{"code":"not_found","message":"Variety var-9 not found"}""",
                HttpStatusCode.NotFound, json)
        }
        server.routes["GET /api/v1/varieties"] = { throw IOException("refused") }
        assertEquals(DataResult.Failure(DataError.NotFound), catalog.variety("var-9"))
        assertEquals(DataResult.Failure(DataError.Offline),
            catalog.varieties("", null, page = 1))
    }

    @Test fun refreshedLotsAreObservable() = runTest {
        server.routes["GET /api/v1/varieties/var-1/lots"] = {
            respond(LOTS, headers = json)
        }
        assertEquals(null, catalog.observeLots("var-1").first())
        catalog.refreshLots("var-1")
        val lots = catalog.observeLots("var-1").first()!!
        assertEquals(listOf(LotStatus.AVAILABLE, LotStatus.ON_LOAN), lots.map { it.status })
    }

    @Test fun borrowingMarksTheLotOnLoanLocally() = runTest {
        server.routes["GET /api/v1/varieties/var-1/lots"] = {
            respond(LOTS, headers = json)
        }
        server.routes["POST /api/v1/loans"] = {
            respond("""{"id":"loan-4","lotId":"lot-1","memberId":"mem-3",
                "borrowedOn":"2026-11-08","dueSeason":2026,"returnedGrams":null}""",
                HttpStatusCode.Created, json)
        }
        catalog.refreshLots("var-1")
        val loan = loans.borrow("lot-1").getOrNull()!!
        assertEquals("loan-4", loan.id)
        assertEquals(LotStatus.ON_LOAN, local.lot("lot-1")?.status)
        assertEquals(2, server.requests.size) // no reload after a success
    }

    @Test fun aConflictReloadsTheLotsOfTheVariety() = runTest {
        server.routes["GET /api/v1/varieties/var-1/lots"] = {
            respond(LOTS, headers = json)
        }
        server.routes["POST /api/v1/loans"] = {
            val body = """{"code":"lot_unavailable","message":"On loan"}"""
            respond(body, HttpStatusCode.Conflict, json)
        }
        catalog.refreshLots("var-1")
        assertEquals(DataResult.Failure(DataError.LotUnavailable),
            loans.borrow("lot-1"))
        assertEquals(listOf(HttpMethod.Get, HttpMethod.Post, HttpMethod.Get),
            server.requests.map { it.method })
    }

    @Test fun myLoansUseTheSessionAndMapUnauthorized() = runTest {
        server.routes["GET /api/v1/members/mem-3/loans"] = {
            respond("""{"code":"unauthorized","message":"Missing token"}""",
                HttpStatusCode.Unauthorized, json)
        }
        assertEquals(DataResult.Failure(DataError.Unauthorized), loans.myLoans())
        assertEquals("demo-token-test",
            server.requests.single().headers["X-Member-Token"])
    }
}
