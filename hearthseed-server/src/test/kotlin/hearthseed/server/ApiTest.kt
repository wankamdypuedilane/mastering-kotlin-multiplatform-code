// hearthseed-server/src/test/kotlin/hearthseed/server/ApiTest.kt
package hearthseed.server

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiTest {
    private val fixedClock =
        Clock.fixed(Instant.parse("2026-11-08T10:00:00Z"), ZoneOffset.UTC)

    private fun api(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) =
        testApplication {
            application { hearthseedModule(LibraryStore.fromResources(fixedClock)) }
            val client = createClient { install(ContentNegotiation) { json() } }
            block(client)
        }

    @Test fun health() = api { client ->
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(HealthDto("ok", 40), response.body())
    }

    @Test fun firstPageOfVarieties() = api { client ->
        val page: Page<VarietyDto> = client.get("/api/v1/varieties").body()
        assertEquals(40, page.total)
        assertEquals(20, page.items.size)
        assertEquals(1, page.page)
        assertEquals("var-001", page.items.first().id)
    }

    @Test fun lastPartialPage() = api { client ->
        val page: Page<VarietyDto> =
            client.get("/api/v1/varieties?page=3&pageSize=15").body()
        assertEquals(10, page.items.size)
        assertEquals("var-031", page.items.first().id)
    }

    @Test fun queryAndFamilyFilters() = api { client ->
        val tomatoes: Page<VarietyDto> =
            client.get("/api/v1/varieties?query=TOMATO").body()
        assertEquals(4, tomatoes.total)
        val latin: Page<VarietyDto> =
            client.get("/api/v1/varieties?query=cucumis").body()
        assertEquals(3, latin.total)
        val brassicas: Page<VarietyDto> =
            client.get("/api/v1/varieties?family=brassicaceae").body()
        assertEquals(6, brassicas.total)
        assertTrue(brassicas.items.all { it.family == "Brassicaceae" })
        val both: Page<VarietyDto> =
            client.get("/api/v1/varieties?query=larkmoor&family=Fabaceae").body()
        assertEquals(listOf("Larkmoor Pea"), both.items.map { it.commonName })
    }

    @Test fun invalidPagingIs400() = api { client ->
        for (q in listOf("page=0", "pageSize=51", "pageSize=x")) {
            val response = client.get("/api/v1/varieties?$q")
            assertEquals(HttpStatusCode.BadRequest, response.status, q)
            assertEquals("bad_request", response.body<ErrorDto>().code)
        }
    }

    @Test fun varietyById() = api { client ->
        val v: VarietyDto = client.get("/api/v1/varieties/var-001").body()
        assertEquals("Larkmoor Ember tomato", v.commonName)
        assertEquals(Difficulty.MEDIUM, v.difficulty)
    }

    @Test fun unknownVarietyIs404WithErrorDto() = api { client ->
        val response = client.get("/api/v1/varieties/var-999")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(ErrorDto("not_found", "Variety var-999 not found"), response.body())
    }

    @Test fun unknownRouteIs404WithErrorDto() = api { client ->
        val response = client.get("/api/v2/nothing")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("not_found", response.body<ErrorDto>().code)
    }

    @Test fun lotsOfVariety() = api { client ->
        val lots: List<SeedLotDto> = client.get("/api/v1/varieties/var-001/lots").body()
        assertEquals(listOf("lot-001", "lot-002"), lots.map { it.id })
        assertEquals(LotStatus.ON_LOAN, lots.first().status)
        assertEquals(HttpStatusCode.NotFound,
            client.get("/api/v1/varieties/var-999/lots").status)
    }

    @Test fun borrowAvailableLotIs201() = api { client ->
        val response = client.post("/api/v1/loans") {
            header(MEMBER_TOKEN_HEADER, "demo-token-ada")
            contentType(ContentType.Application.Json)
            setBody(LoanRequest("lot-002", "mem-001"))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val loan: LoanDto = response.body()
        assertEquals(LoanDto("loan-004", "lot-002", "mem-001", "2026-11-08", 2026), loan)
        val lots: List<SeedLotDto> = client.get("/api/v1/varieties/var-001/lots").body()
        assertEquals(LotStatus.ON_LOAN, lots.single { it.id == "lot-002" }.status)
    }

    @Test fun borrowLotOnLoanIs409() = api { client ->
        val response = client.post("/api/v1/loans") {
            header(MEMBER_TOKEN_HEADER, "demo-token-ada")
            contentType(ContentType.Application.Json)
            setBody(LoanRequest("lot-001", "mem-002"))
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals("lot_unavailable", response.body<ErrorDto>().code)
    }

    @Test fun borrowUnknownLotIs404() = api { client ->
        val response = client.post("/api/v1/loans") {
            header(MEMBER_TOKEN_HEADER, "demo-token-ada")
            contentType(ContentType.Application.Json)
            setBody(LoanRequest("lot-999", "mem-001"))
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test fun malformedBodyIs400() = api { client ->
        val response = client.post("/api/v1/loans") {
            header(MEMBER_TOKEN_HEADER, "demo-token-ada")
            contentType(ContentType.Application.Json)
            setBody("""{"lotId": 3}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test fun missingOrWrongTokenIs401() = api { client ->
        val noToken = client.post("/api/v1/loans") {
            contentType(ContentType.Application.Json)
            setBody(LoanRequest("lot-002", "mem-001"))
        }
        assertEquals(HttpStatusCode.Unauthorized, noToken.status)
        assertEquals("unauthorized", noToken.body<ErrorDto>().code)
        for (token in listOf("YOUR_MEMBER_TOKEN", "demo-token-")) {
            val wrong = client.get("/api/v1/members/mem-001/loans") {
                header(MEMBER_TOKEN_HEADER, token)
            }
            assertEquals(HttpStatusCode.Unauthorized, wrong.status, token)
        }
    }

    @Test fun loansOfMember() = api { client ->
        val loans: List<LoanDto> = client.get("/api/v1/members/mem-001/loans") {
            header(MEMBER_TOKEN_HEADER, "demo-token-ada")
        }.body()
        assertEquals(listOf("loan-001", "loan-003"), loans.map { it.id })
        assertEquals(40, loans.last().returnedGrams)
        val unknown = client.get("/api/v1/members/mem-999/loans") {
            header(MEMBER_TOKEN_HEADER, "demo-token-ada")
        }
        assertEquals(HttpStatusCode.NotFound, unknown.status)
    }

    @Test fun nullsAreExplicitInJson() = api { client ->
        val text = client.get("/api/v1/varieties/var-001/lots").bodyAsText()
        assertTrue("\"donorMemberId\":null" in text, text)
    }
}
