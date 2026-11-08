// hearthseed/core/data/src/commonMain/kotlin/hearthseed/core/data/DataResult.kt
package hearthseed.core.data

/** Every failure a Hearthseed screen may have to explain to a member. */
sealed interface DataError {
    /** No connection or no answer in time: worth a "Retry" button. */
    data object Offline : DataError
    data object NotFound : DataError
    /** 409: the lot was borrowed (or retired) by someone else first. */
    data object LotUnavailable : DataError
    /** 401: the member token is missing or refused. */
    data object Unauthorized : DataError
    data class Server(val status: Int) : DataError
    /** A bug or a broken response: bad request, invalid JSON or data. */
    data class Unexpected(val reason: String) : DataError
}

/** The outcome of a repository call: a value or a typed error. */
sealed interface DataResult<out T> {
    data class Success<out T>(val value: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> =
    when (this) {
        is DataResult.Success -> DataResult.Success(transform(value))
        is DataResult.Failure -> this
    }

fun <T> DataResult<T>.getOrNull(): T? = (this as? DataResult.Success)?.value
