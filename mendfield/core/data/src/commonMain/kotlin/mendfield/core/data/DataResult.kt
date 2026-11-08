// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/DataResult.kt
package mendfield.core.data

import mendfield.core.network.ApiError

/** What the screens need to know about a failure, without HTTP. */
sealed interface DataError {
    data object Offline : DataError
    data object Unauthorized : DataError
    data object NotFound : DataError
    data object Server : DataError
    data class Unexpected(val detail: String) : DataError
}

sealed interface DataResult<out T> {
    data class Success<T>(val value: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}

fun ApiError.toDataError(): DataError = when (this) {
    is ApiError.Network, ApiError.Timeout -> DataError.Offline
    is ApiError.Unauthorized -> DataError.Unauthorized
    is ApiError.NotFound -> DataError.NotFound
    is ApiError.Server -> DataError.Server
    is ApiError.BadRequest -> DataError.Unexpected(message)
    is ApiError.Conflict -> DataError.Unexpected(message)
    is ApiError.Unexpected -> DataError.Unexpected(message)
}

/** One sentence for the organizer; the screens add a Retry button. */
fun DataError.userMessage(): String = when (this) {
    DataError.Offline ->
        "Can't reach the café server. Showing what this device knows."
    DataError.Unauthorized -> "The organizer token was refused."
    DataError.NotFound -> "The server doesn't know this event any more."
    DataError.Server -> "The café server has a problem. Try again soon."
    is DataError.Unexpected -> "Something went wrong: $detail"
}
