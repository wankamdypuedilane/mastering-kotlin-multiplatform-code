// ch21-production/secure/src/iosTest/kotlin/hearthseed/secure/KeychainSecureStoreTest.kt
package hearthseed.secure

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class KeychainSecureStoreTest {
    private val store = KeychainSecureStore(service = "hearthseed.tests")

    /**
     * The Kotlin/Native test binary runs in the simulator without an app
     * bundle. If the Keychain refuses it (errSecMissingEntitlement, -34018),
     * the round trip cannot run there: the test says so instead of failing.
     */
    @Test
    fun writeReadUpdateDelete() = runTest {
        try {
            store.write("session", "YOUR_MEMBER_TOKEN")
        } catch (e: KeychainException) {
            if (e.status == MISSING_ENTITLEMENT) {
                println("Keychain unavailable to the test binary: ${e.message}")
                return@runTest
            }
            throw e
        }
        assertEquals("YOUR_MEMBER_TOKEN", store.read("session"))
        store.write("session", "YOUR_OTHER_TOKEN") // errSecDuplicateItem path
        assertEquals("YOUR_OTHER_TOKEN", store.read("session"))
        store.delete("session")
        assertNull(store.read("session"))
        store.delete("session") // deleting twice is not an error
    }

    @Test
    fun theKeychainIsThePlatformStore() {
        assertEquals(SecureStorageKind.APPLE_KEYCHAIN, secureStorageKind)
    }

    private companion object {
        const val MISSING_ENTITLEMENT = -34018
    }
}
