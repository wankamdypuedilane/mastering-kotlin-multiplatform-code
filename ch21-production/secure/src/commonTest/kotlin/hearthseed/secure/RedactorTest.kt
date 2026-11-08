// ch21-production/secure/src/commonTest/kotlin/hearthseed/secure/RedactorTest.kt
package hearthseed.secure

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RedactorTest {
    @Test
    fun headersQueryParametersAndJsonAreMasked() {
        val line = "POST /api/v1/loans?token=demo-token-cleo&page=2 " +
            "X-Member-Token: demo-token-cleo Authorization: Bearer abc.def " +
            """{"lotId":"lot-006","refreshToken":"demo-token-cleo"}"""
        val clean = Redactor.redact(line)
        assertFalse("demo-token-cleo" in clean)
        assertFalse("abc.def" in clean)
        assertEquals(
            "POST /api/v1/loans?token=<redacted>&page=2 " +
                "X-Member-Token: <redacted> Authorization: Bearer <redacted> " +
                """{"lotId":"lot-006","refreshToken":"<redacted>"}""",
            clean,
        )
    }

    @Test
    fun ordinaryTextIsUntouched() {
        val line = "GET /api/v1/varieties?query=pea&page=1 -> 200"
        assertEquals(line, Redactor.redact(line))
    }

    @Test
    fun theLoggerRedactsBeforeAnyWriterSeesTheMessage() {
        val captured = CapturingWriter()
        val log = appLogger(release = false, writers = listOf(captured))
        log.i { "Signed in with X-Member-Token: demo-token-cleo" }
        log.d { "Session saved: ${Session("mem-003", "demo-token-cleo", 0)}" }
        assertEquals(
            listOf(
                "Signed in with X-Member-Token: <redacted>",
                "Session saved: Session(memberId=mem-003, token=<redacted>, " +
                    "expiresAtEpochSeconds=0)",
            ),
            captured.messages,
        )
    }

    @Test
    fun releaseBuildsDropDebugMessages() {
        val captured = CapturingWriter()
        val log = appLogger(release = true, writers = listOf(captured))
        log.d { "cache hit" }
        log.w { "slow response" }
        assertEquals(listOf("slow response"), captured.messages)
    }
}

class CapturingWriter : LogWriter() {
    val messages = mutableListOf<String>()

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        messages += message
    }
}
