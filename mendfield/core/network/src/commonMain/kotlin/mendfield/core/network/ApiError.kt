// mendfield/core/network/src/commonMain/kotlin/mendfield/core/network/ApiError.kt
package mendfield.core.network

import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/** What can go wrong, in the words of the app rather than of HTTP. */
sealed interface ApiError {
    data class NotFound(val message: String) : ApiError
    /** A repair-café rule said no: [code] is "slot_taken", "visitor_busy"... */
    data class Conflict(val code: String, val message: String) : ApiError
    data class Unauthorized(val message: String) : ApiError
    data class BadRequest(val message: String) : ApiError
    data class Server(val status: Int) : ApiError
    data object Timeout : ApiError
    data class Network(val message: String) : ApiError
    data class Unexpected(val message: String) : ApiError

    /** Worth trying again later: the request may never have arrived. */
    val isTransient: Boolean
        get() = this is Timeout || this is Network || this is Server
}

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

/** Runs one API call (request and mapping); failures become [ApiError]s. */
internal suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: CancellationException) {
        throw e // never swallow cancellation
    } catch (e: Throwable) {
        ApiResult.Failure(e.toApiError())
    }

internal suspend fun Throwable.toApiError(): ApiError = when (this) {
    // expectSuccess = true: every non-2xx response throws ResponseException.
    is ResponseException -> {
        val status = response.status
        val error = errorBodyOrNull()
        val message = error?.message ?: status.description
        when {
            status == HttpStatusCode.NotFound -> ApiError.NotFound(message)
            status == HttpStatusCode.Conflict ->
                ApiError.Conflict(error?.code ?: "conflict", message)
            status == HttpStatusCode.Unauthorized ->
                ApiError.Unauthorized(message)
            status == HttpStatusCode.BadRequest -> ApiError.BadRequest(message)
            status.value >= 500 -> ApiError.Server(status.value)
            else -> ApiError.Unexpected("HTTP ${status.value}: $message")
        }
    }
    is HttpRequestTimeoutException, is ConnectTimeoutException,
    is SocketTimeoutException -> ApiError.Timeout
    is IOException -> ApiError.Network(message ?: "Network error")
    is SerializationException -> ApiError.Unexpected("Bad JSON: $message")
    // Parsing of dates, enums and time zones in the mappers.
    is IllegalArgumentException -> ApiError.Unexpected("Bad data: $message")
    else -> ApiError.Unexpected(message ?: "Unknown error")
}

private suspend fun ResponseException.errorBodyOrNull(): ErrorDto? =
    try {
        response.body<ErrorDto>()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null // not our JSON error shape (a proxy page, an empty body...)
    }
