package hearthseed.data.repository

/** Every failure the UI layer may have to explain to a member. */
sealed interface DataError {
    data object Offline : DataError
    data object NotFound : DataError
    data class Server(val status: Int) : DataError
    data class InvalidData(val reason: String) : DataError
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
