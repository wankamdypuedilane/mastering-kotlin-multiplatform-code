// ch21-production/secure/src/iosTest/kotlin/hearthseed/secure/KeychainSecureStoreTest.kt
package hearthseed.secure

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import platform.Security.errSecMissingEntitlement
import platform.Security.errSecNotAvailable

class KeychainSecureStoreTest {
    private val store = KeychainSecureStore(service = "hearthseed.tests")

    /**
     * The Kotlin/Native test binary runs in the simulator without an app
     * bundle. If the Keychain refuses such a process (missing entitlement,
     * no keychain available), the round trip cannot run there: the test
     * prints the status and stops instead of failing. Any other status fails.
     */
    @Test
    fun writeReadUpdateDelete() = runTest {
        try {
            store.write("session", "YOUR_MEMBER_TOKEN")
        } catch (e: KeychainException) {
            println("Keychain write: ${e.message}")
            if (e.status in noKeychainForThisProcess) return@runTest
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

    private val noKeychainForThisProcess =
        setOf(errSecMissingEntitlement, errSecNotAvailable)
}
