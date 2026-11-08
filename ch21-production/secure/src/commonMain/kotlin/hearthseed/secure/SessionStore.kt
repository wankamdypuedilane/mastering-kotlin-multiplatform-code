// ch21-production/secure/src/commonMain/kotlin/hearthseed/secure/SessionStore.kt
package hearthseed.secure

import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** What the app keeps after sign-in. The token is a fictional placeholder. */
@Serializable
data class Session(
    val memberId: String,
    val token: String,
    val expiresAtEpochSeconds: Long,
) {
    // A data class prints every property: keep the token out of logs.
    override fun toString(): String =
        "Session(memberId=$memberId, token=<redacted>, " +
            "expiresAtEpochSeconds=$expiresAtEpochSeconds)"
}

/** Saves the session as JSON inside a [SecureStore]. */
class SessionStore(private val store: SecureStore) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun save(session: Session) =
        store.write(KEY, json.encodeToString(Session.serializer(), session))

    /** The saved session, or null; unreadable data is deleted. */
    suspend fun load(): Session? {
        val text = store.read(KEY) ?: return null
        return try {
            json.decodeFromString(Session.serializer(), text)
        } catch (e: SerializationException) {
            store.delete(KEY)
            null
        }
    }

    suspend fun clear() = store.delete(KEY)

    private companion object {
        const val KEY = "session"
    }
}
