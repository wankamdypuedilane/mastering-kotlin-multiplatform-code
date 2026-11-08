// hearthseed/core/network/src/commonMain/kotlin/hearthseed/core/network/HearthseedApi.kt
package hearthseed.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

/** The Hearthseed test API, one suspend function per endpoint. */
class HearthseedApi(
    private val client: HttpClient,
    private val memberToken: String,
) {
    suspend fun health(): ApiResult<HealthDto> = apiCall {
        client.get("/health").body() // leading "/": outside /api/v1/
    }

    suspend fun varieties(
        query: String? = null,
        family: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): ApiResult<Page<VarietyDto>> = apiCall {
        client.get("varieties") {
            parameter("query", query) // null values are left out
            parameter("family", family)
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun variety(id: String): ApiResult<VarietyDto> = apiCall {
        client.get { path("varieties", id) }.body()
    }

    suspend fun lots(varietyId: String): ApiResult<List<SeedLotDto>> = apiCall {
        client.get { path("varieties", varietyId, "lots") }.body()
    }

    suspend fun borrow(lotId: String, memberId: String): ApiResult<LoanDto> =
        apiCall {
            client.post("loans") {
                header(MEMBER_TOKEN_HEADER, memberToken)
                contentType(ContentType.Application.Json)
                setBody(LoanRequest(lotId, memberId))
            }.body()
        }

    suspend fun loansOf(memberId: String): ApiResult<List<LoanDto>> = apiCall {
        client.get {
            path("members", memberId, "loans")
            header(MEMBER_TOKEN_HEADER, memberToken)
        }.body()
    }

    // encodeSlash = true: an id such as "a/../b" stays one segment.
    private fun HttpRequestBuilder.path(vararg segments: String) {
        url { appendPathSegments(*segments, encodeSlash = true) }
    }
}
