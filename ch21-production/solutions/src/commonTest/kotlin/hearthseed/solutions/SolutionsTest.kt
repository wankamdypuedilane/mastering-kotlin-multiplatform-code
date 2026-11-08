// ch21-production/solutions/src/commonTest/kotlin/hearthseed/solutions/SolutionsTest.kt
package hearthseed.solutions

import hearthseed.secure.InMemorySecureStore
import hearthseed.secure.Session
import hearthseed.secure.SessionStore
import hearthseed.secure.Validated
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

class SolutionsTest {
    @Test
    fun emailsAndTokensAreBothMasked() {
        assertEquals(
            "Reminder sent to c***@larkmoor.example (X-Member-Token: <redacted>)",
            redactForLogs("Reminder sent to cleo@larkmoor.example (X-Member-Token: demo-token-cleo)"),
        )
    }

    @Test
    fun returnedGramsAcceptsAUnitAndRejectsNonsense() {
        assertEquals(Validated.Valid(120), returnedGrams(" 120 g"))
        assertTrue(returnedGrams("12.5") is Validated.Invalid)
        assertTrue(returnedGrams("0") is Validated.Invalid)
        assertTrue(returnedGrams("5001") is Validated.Invalid)
    }

    @Test
    fun sessionsExpireWithAMargin() = runTest {
        val store = SessionStore(InMemorySecureStore())
        store.save(Session("mem-003", "YOUR_MEMBER_TOKEN", expiresAtEpochSeconds = 1_000))
        val early = ExpiringSessions(store, FixedClock(900))
        assertEquals("mem-003", early.current()?.memberId)
        val late = ExpiringSessions(store, FixedClock(980)) // inside the 30 s margin
        assertNull(late.current())
        assertNull(store.load()) // and it was cleared
    }
}

private class FixedClock(private val epochSeconds: Long) : Clock {
    override fun now(): Instant = Instant.fromEpochSeconds(epochSeconds)
}
