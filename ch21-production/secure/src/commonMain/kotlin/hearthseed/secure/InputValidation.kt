// ch21-production/secure/src/commonMain/kotlin/hearthseed/secure/InputValidation.kt
package hearthseed.secure

/** One problem with one field, phrased for the person filling the form. */
data class FieldError(val field: String, val message: String)

sealed interface Validated<out T> {
    data class Valid<T>(val value: T) : Validated<T>
    data class Invalid(val errors: List<FieldError>) : Validated<Nothing>
}

/** The body of POST /api/v1/loans, built only from checked input. */
data class LoanRequest(val lotId: String, val memberId: String)

object InputValidator {
    private val lotId = Regex("lot-[0-9]{3,6}")
    private val memberId = Regex("mem-[0-9]{3,6}")
    const val MAX_QUERY_LENGTH = 60

    fun loanRequest(rawLot: String, rawMember: String): Validated<LoanRequest> {
        val lot = rawLot.trim()
        val member = rawMember.trim()
        val errors = buildList {
            if (!lotId.matches(lot)) add(FieldError("lotId", "Unknown seed lot"))
            if (!memberId.matches(member)) {
                add(FieldError("memberId", "Unknown member"))
            }
        }
        return if (errors.isEmpty()) Validated.Valid(LoanRequest(lot, member))
        else Validated.Invalid(errors)
    }

    /** Trims, collapses spaces, rejects control characters and long text. */
    fun searchQuery(raw: String): Validated<String> {
        val query = raw.trim().replace(Regex("""\s+"""), " ")
        val error = when {
            query.any { it.isISOControl() } -> "Remove special characters"
            query.length > MAX_QUERY_LENGTH ->
                "Use at most $MAX_QUERY_LENGTH characters"
            else -> return Validated.Valid(query)
        }
        return Validated.Invalid(listOf(FieldError("query", error)))
    }
}
