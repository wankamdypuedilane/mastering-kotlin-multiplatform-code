// hearthseed/core/data/src/commonMain/kotlin/hearthseed/core/data/UserMessages.kt
package hearthseed.core.data

/** One sentence per error, for every screen (a `when` without `else`). */
fun DataError.userMessage(): String = when (this) {
    DataError.Offline -> "Can't reach the seed library. Check your connection."
    DataError.NotFound -> "This item no longer exists in the library."
    DataError.LotUnavailable -> "Another member borrowed this lot a moment ago."
    DataError.Unauthorized -> "Your member token was refused. Sign in again."
    is DataError.Server ->
        "The library server had a problem ($status). Try again later."
    is DataError.Unexpected -> "Something went wrong on our side."
}
