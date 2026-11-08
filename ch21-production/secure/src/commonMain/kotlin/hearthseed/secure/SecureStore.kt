// ch21-production/secure/src/commonMain/kotlin/hearthseed/secure/SecureStore.kt
package hearthseed.secure

/**
 * A small key-value store for secrets such as session tokens.
 * Functions suspend because platform stores may block (Keystore, Keychain).
 */
interface SecureStore {
    suspend fun read(key: String): String?
    suspend fun write(key: String, value: String)
    suspend fun delete(key: String)
}

/** Where a platform keeps secrets; common code adapts its behavior to it. */
enum class SecureStorageKind { ANDROID_KEYSTORE, APPLE_KEYCHAIN, MEMORY_ONLY }

/** The storage this platform offers in this module. */
expect val secureStorageKind: SecureStorageKind

/** Offer "stay signed in" only where secrets survive a restart safely. */
val canRememberSession: Boolean
    get() = secureStorageKind != SecureStorageKind.MEMORY_ONLY

/** Values live in memory only and disappear with the process. */
class InMemorySecureStore : SecureStore {
    private val values = mutableMapOf<String, String>()

    override suspend fun read(key: String): String? = values[key]

    override suspend fun write(key: String, value: String) {
        values[key] = value
    }

    override suspend fun delete(key: String) {
        values.remove(key)
    }
}
