// ch21-production/solutions/src/commonMain/kotlin/hearthseed/solutions/ReturnValidation.kt
package hearthseed.solutions

import hearthseed.secure.FieldError
import hearthseed.secure.Validated

const val MAX_RETURN_GRAMS = 5_000

/** Exercise 21.2: the returned weight, typed by a member, as whole grams. */
fun returnedGrams(raw: String): Validated<Int> {
    val grams = raw.trim().removeSuffix("g").trim().toIntOrNull()
    val error = when {
        grams == null -> "Enter a whole number of grams"
        grams < 1 -> "Return at least 1 g"
        grams > MAX_RETURN_GRAMS -> "At most $MAX_RETURN_GRAMS g per return"
        else -> return Validated.Valid(grams)
    }
    return Validated.Invalid(listOf(FieldError("returnedGrams", error)))
}
