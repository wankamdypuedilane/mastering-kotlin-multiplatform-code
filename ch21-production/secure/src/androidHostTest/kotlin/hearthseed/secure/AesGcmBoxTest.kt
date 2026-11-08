// ch21-production/secure/src/androidHostTest/kotlin/hearthseed/secure/AesGcmBoxTest.kt
package hearthseed.secure

import java.security.GeneralSecurityException
import javax.crypto.KeyGenerator
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

// Host tests run on the JVM: a JCE key stands in for the Keystore key.
class AesGcmBoxTest {
    private val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    private val box = AesGcmBox { key }

    @Test
    fun sealedTextOpensAgain() {
        assertEquals("YOUR_MEMBER_TOKEN", box.open(box.seal("YOUR_MEMBER_TOKEN")))
    }

    @Test
    fun sameTextSealsDifferentlyEachTime() {
        val sealed = box.seal("YOUR_MEMBER_TOKEN")
        assertNotEquals(sealed, box.seal("YOUR_MEMBER_TOKEN"))
        assertFalse("YOUR_MEMBER_TOKEN" in Base64.decode(sealed).decodeToString())
    }

    @Test
    fun tamperingIsDetected() {
        val bytes = Base64.decode(box.seal("YOUR_MEMBER_TOKEN"))
        bytes[bytes.lastIndex] = (bytes.last() + 1).toByte()
        assertFailsWith<GeneralSecurityException> { box.open(Base64.encode(bytes)) }
    }

    @Test
    fun anotherKeyCannotOpen() {
        val other = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        val sealed = box.seal("YOUR_MEMBER_TOKEN")
        assertFailsWith<GeneralSecurityException> { AesGcmBox { other }.open(sealed) }
    }
}
