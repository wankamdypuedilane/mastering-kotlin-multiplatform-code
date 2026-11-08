// ch21-production/secure/src/commonTest/kotlin/hearthseed/secure/SessionStoreTest.kt
package hearthseed.secure

import hearthseed.config.AppConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class SessionStoreTest {
    private val session = Session("mem-003", "YOUR_MEMBER_TOKEN", 1_800_000_000)

    @Test
    fun aSavedSessionLoadsBack() = runTest {
        val sessions = SessionStore(InMemorySecureStore())
        sessions.save(session)
        assertEquals(session, sessions.load())
        sessions.clear()
        assertNull(sessions.load())
    }

    @Test
    fun corruptDataIsDeletedNotThrown() = runTest {
        val store = InMemorySecureStore()
        store.write("session", "{not json")
        assertNull(SessionStore(store).load())
        assertNull(store.read("session"))
    }

    @Test
    fun generatedConfigComesFromGradleProperties() {
        assertEquals("https://api.larkmoor.example/v1", AppConfig.API_BASE_URL)
        assertEquals(15, AppConfig.REQUEST_TIMEOUT_SECONDS)
        assertEquals("staging", AppConfig.ENVIRONMENT)
    }
}
