package language

// Section 2.9: error handling.

fun germinationPercent(sown: Int, sprouted: Int): Int {
    require(sown > 0) { "sown must be positive, was $sown" }
    require(sprouted in 0..sown) { "sprouted must be in 0..$sown" }
    return sprouted * 100 / sown
}

class SeedDrawer {
    private var open = false
    fun open() {
        open = true
    }

    fun take(): String {
        check(open) { "Open the drawer first" }
        return "packet"
    }
}

fun packetsFrom(text: String): Int =
    try {
        text.trim().toInt()
    } catch (e: NumberFormatException) {
        0
    }

fun safePercent(sown: Int, sprouted: Int): Result<Int> =
    runCatching { germinationPercent(sown, sprouted) }

sealed interface CountInput {
    data class Valid(val value: Int) : CountInput
    data object Blank : CountInput
    data class NotANumber(val text: String) : CountInput
    data class Negative(val value: Int) : CountInput
}

fun readCount(text: String): CountInput {
    if (text.isBlank()) return CountInput.Blank
    val number = text.trim().toIntOrNull()
        ?: return CountInput.NotANumber(text)
    return if (number < 0) CountInput.Negative(number)
    else CountInput.Valid(number)
}

fun feedback(input: CountInput): String = when (input) {
    is CountInput.Valid -> "${input.value} packets"
    CountInput.Blank -> "Enter a number"
    is CountInput.NotANumber -> "'${input.text}' is not a number"
    is CountInput.Negative -> "Counts cannot be negative"
}
