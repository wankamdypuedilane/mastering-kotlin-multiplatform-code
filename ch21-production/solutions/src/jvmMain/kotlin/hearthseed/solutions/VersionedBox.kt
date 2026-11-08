// ch21-production/solutions/src/jvmMain/kotlin/hearthseed/solutions/VersionedBox.kt
package hearthseed.solutions

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64

/**
 * Exercise 21.4: AES-GCM with key rotation. Each value starts with the
 * version of its key ("v2:..."); old values still open, new ones use
 * [current], and [needsRewrap] finds values to seal again.
 */
class VersionedBox(
    private val keys: Map<Int, SecretKey>,
    private val current: Int,
) {
    init {
        require(current in keys) { "No key for current version $current" }
    }

    fun seal(plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keys.getValue(current))
        // The version is authenticated too: changing it breaks the tag.
        cipher.updateAAD(header(current).encodeToByteArray())
        val body = cipher.iv + cipher.doFinal(plain.encodeToByteArray())
        return header(current) + Base64.encode(body)
    }

    fun open(sealed: String): String {
        val version = versionOf(sealed)
        val key = keys[version]
            ?: throw IllegalArgumentException("Unknown key v$version")
        val bytes = Base64.decode(sealed.substringAfter(':'))
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, bytes, 0, 12)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        cipher.updateAAD(header(version).encodeToByteArray())
        return cipher.doFinal(bytes, 12, bytes.size - 12).decodeToString()
    }

    fun needsRewrap(sealed: String): Boolean = versionOf(sealed) != current

    private fun header(version: Int) = "v$version:"

    private fun versionOf(sealed: String): Int =
        sealed.substringBefore(':', "").removePrefix("v").toIntOrNull()
            ?: throw IllegalArgumentException("Missing key version")
}
