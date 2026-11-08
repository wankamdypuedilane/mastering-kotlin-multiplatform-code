// ch09-ktor/client/src/commonMain/kotlin/hearthseed/network/ApiError.kt
package hearthseed.network

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
    data class Conflict(val message: String) : ApiError
    data class Unauthorized(val message: String) : ApiError
    data class BadRequest(val message: String) : ApiError
    data class Server(val status: Int) : ApiError
    data object Timeout : ApiError
    data class Network(val message: String) : ApiError
    data class Unexpected(val message: String) : ApiError
}

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

/** Runs one API call and turns every failure into an [ApiError]. */
internal suspend fun <T> apiCall(block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: CancellationException) {
        throw e // never swallow cancellation (Chapter 4)
    } catch (e: Throwable) {
        ApiResult.Failure(e.toApiError())
    }

internal suspend fun Throwable.toApiError(): ApiError = when (this) {
    // expectSuccess = true: every non-2xx response throws ResponseException.
    is ResponseException -> {
        val status = response.status
        val message = errorBodyOrNull()?.message ?: status.description
        when {
            status == HttpStatusCode.NotFound -> ApiError.NotFound(message)
            status == HttpStatusCode.Conflict -> ApiError.Conflict(message)
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
