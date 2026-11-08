// ch21-production/solutions/src/jvmTest/kotlin/hearthseed/solutions/VersionedBoxTest.kt
package hearthseed.solutions

import java.security.GeneralSecurityException
import javax.crypto.KeyGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersionedBoxTest {
    private fun newKey() = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val k1 = newKey()
    private val k2 = newKey()

    @Test
    fun oldValuesOpenAfterRotationAndAreFlaggedForRewrap() {
        val old = VersionedBox(mapOf(1 to k1), current = 1).seal("YOUR_MEMBER_TOKEN")
        val rotated = VersionedBox(mapOf(1 to k1, 2 to k2), current = 2)
        assertEquals("YOUR_MEMBER_TOKEN", rotated.open(old))
        assertTrue(rotated.needsRewrap(old))
        val fresh = rotated.seal("YOUR_MEMBER_TOKEN")
        assertTrue(fresh.startsWith("v2:"))
        assertFalse(rotated.needsRewrap(fresh))
    }

    @Test
    fun aRetiredKeyCannotOpenAndAForgedVersionFails() {
        val old = VersionedBox(mapOf(1 to k1), current = 1).seal("YOUR_MEMBER_TOKEN")
        val onlyNew = VersionedBox(mapOf(2 to k2), current = 2)
        assertFailsWith<IllegalArgumentException> { onlyNew.open(old) }
        val both = VersionedBox(mapOf(1 to k1, 2 to k1), current = 2)
        val forged = "v1:" + both.seal("YOUR_MEMBER_TOKEN").substringAfter(':')
        assertFailsWith<GeneralSecurityException> { both.open(forged) }
    }
}
