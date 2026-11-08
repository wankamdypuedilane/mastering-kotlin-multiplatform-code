package seedswap.core

/** What the user typed, exactly as typed: every field is text. */
data class ProposalDraft(
    val member: String = "",
    val variety: String = "",
    val grams: String = "",
    val message: String = "",
)

enum class ProposalField { MEMBER, VARIETY, GRAMS, MESSAGE }

enum class FieldError { REQUIRED, NOT_A_NUMBER, OUT_OF_RANGE, TOO_LONG }

object ProposalRules {
    const val MIN_GRAMS = 1
    const val MAX_GRAMS = 500
    const val MAX_MESSAGE = 200
}

sealed interface DraftCheck {
    data class Valid(val proposal: Proposal) : DraftCheck
    data class Invalid(val errors: Map<ProposalField, FieldError>) : DraftCheck
}

/** Pure validation: no UI, no coroutines, testable on every target. */
fun ProposalDraft.check(offerId: String): DraftCheck {
    val parsedGrams = grams.trim().toIntOrNull()
    val errors = buildMap {
        if (member.isBlank()) put(ProposalField.MEMBER, FieldError.REQUIRED)
        if (variety.isBlank()) put(ProposalField.VARIETY, FieldError.REQUIRED)
        when {
            grams.isBlank() -> put(ProposalField.GRAMS, FieldError.REQUIRED)
            parsedGrams == null ->
                put(ProposalField.GRAMS, FieldError.NOT_A_NUMBER)
            parsedGrams !in ProposalRules.MIN_GRAMS..ProposalRules.MAX_GRAMS ->
                put(ProposalField.GRAMS, FieldError.OUT_OF_RANGE)
        }
        if (message.length > ProposalRules.MAX_MESSAGE) {
            put(ProposalField.MESSAGE, FieldError.TOO_LONG)
        }
    }
    if (errors.isNotEmpty() || parsedGrams == null) {
        return DraftCheck.Invalid(errors)
    }
    return DraftCheck.Valid(
        Proposal(offerId, member.trim(), variety.trim(), parsedGrams,
            message.trim()),
    )
}
