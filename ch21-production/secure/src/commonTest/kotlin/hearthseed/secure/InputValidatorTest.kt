// ch21-production/secure/src/commonTest/kotlin/hearthseed/secure/InputValidatorTest.kt
package hearthseed.secure

import kotlin.test.Test
import kotlin.test.assertEquals

class InputValidatorTest {
    @Test
    fun aWellFormedLoanRequestIsTrimmedAndAccepted() {
        assertEquals(
            Validated.Valid(LoanRequest("lot-006", "mem-003")),
            InputValidator.loanRequest(" lot-006 ", "mem-003"),
        )
    }

    @Test
    fun everyBadFieldIsReported() {
        val result = InputValidator.loanRequest("lot-6; DROP TABLE loan", "")
        assertEquals(
            Validated.Invalid(
                listOf(
                    FieldError("lotId", "Unknown seed lot"),
                    FieldError("memberId", "Unknown member"),
                ),
            ),
            result,
        )
    }

    @Test
    fun searchQueriesAreNormalized() {
        assertEquals(Validated.Valid("larkmoor pea"), InputValidator.searchQuery("  larkmoor \t pea "))
    }

    @Test
    fun controlCharactersAndLongQueriesAreRejected() {
        val nul = InputValidator.searchQuery("pea\u0000")
        val long = InputValidator.searchQuery("p".repeat(61))
        assertEquals("query", (nul as Validated.Invalid).errors.single().field)
        assertEquals(
            "Use at most 60 characters",
            (long as Validated.Invalid).errors.single().message,
        )
    }
}
