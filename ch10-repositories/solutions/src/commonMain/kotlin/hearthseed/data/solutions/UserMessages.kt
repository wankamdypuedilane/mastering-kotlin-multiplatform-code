package hearthseed.data.solutions

import hearthseed.data.repository.DataError

/** Exercise 10.1: one message per error; the compiler checks every case. */
fun DataError.userMessage(): String = when (this) {
    DataError.Offline -> "You are offline. Showing saved data if there is any."
    DataError.NotFound -> "This item is no longer in the library catalog."
    is DataError.Server ->
        "The library server had a problem ($status). Try again later."
    is DataError.InvalidData -> "The library sent data this app cannot read."
}
