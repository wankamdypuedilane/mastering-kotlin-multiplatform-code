// ch21-production/solutions/src/commonMain/kotlin/hearthseed/solutions/ExpiringSessions.kt
package hearthseed.solutions

import hearthseed.secure.Session
import hearthseed.secure.SessionStore
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Exercise 21.3: a session close to its expiry is as good as expired. */
class ExpiringSessions(
    private val sessions: SessionStore,
    private val clock: Clock,
    private val margin: Duration = 30.seconds,
) {
    suspend fun current(): Session? {
        val session = sessions.load() ?: return null
        val deadline = session.expiresAtEpochSeconds - margin.inWholeSeconds
        if (clock.now().epochSeconds < deadline) return session
        sessions.clear() // never send a token the server will refuse
        return null
    }
}
