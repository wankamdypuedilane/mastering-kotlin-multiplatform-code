// ch21-production/secure/src/jvmTest/kotlin/hearthseed/secure/SecureStorageKindTest.kt
package hearthseed.secure

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SecureStorageKindTest {
    @Test
    fun desktopKeepsSecretsInMemoryOnly() {
        assertEquals(SecureStorageKind.MEMORY_ONLY, secureStorageKind)
        assertFalse(canRememberSession)
    }
}
