package seedswap.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProposalValidationTest {
    private val good = ProposalDraft(
        member = " Lena Moss ", variety = "Genovese basil", grams = "10", message = "",
    )

    @Test
    fun validDraftBecomesATrimmedProposal() {
        val check = assertIs<DraftCheck.Valid>(good.check("o4"))
        assertEquals(Proposal("o4", "Lena Moss", "Genovese basil", 10, ""), check.proposal)
    }

    @Test
    fun emptyDraftReportsEveryRequiredField() {
        val check = assertIs<DraftCheck.Invalid>(ProposalDraft().check("o4"))
        assertEquals(
            mapOf(
                ProposalField.MEMBER to FieldError.REQUIRED,
                ProposalField.VARIETY to FieldError.REQUIRED,
                ProposalField.GRAMS to FieldError.REQUIRED,
            ),
            check.errors,
        )
    }

    @Test
    fun gramsMustBeAWholeNumberInRange() {
        fun gramsError(text: String) =
            (good.copy(grams = text).check("o4") as? DraftCheck.Invalid)
                ?.errors?.get(ProposalField.GRAMS)

        assertEquals(FieldError.NOT_A_NUMBER, gramsError("ten"))
        assertEquals(FieldError.NOT_A_NUMBER, gramsError("2.5"))
        assertEquals(FieldError.OUT_OF_RANGE, gramsError("0"))
        assertEquals(FieldError.OUT_OF_RANGE, gramsError("501"))
        assertEquals(null, gramsError("500"))
        assertEquals(null, gramsError(" 1 "))
    }

    @Test
    fun messageHasAMaximumLength() {
        val long = good.copy(message = "a".repeat(ProposalRules.MAX_MESSAGE + 1))
        val check = assertIs<DraftCheck.Invalid>(long.check("o4"))
        assertEquals(mapOf(ProposalField.MESSAGE to FieldError.TOO_LONG), check.errors)
    }
}
