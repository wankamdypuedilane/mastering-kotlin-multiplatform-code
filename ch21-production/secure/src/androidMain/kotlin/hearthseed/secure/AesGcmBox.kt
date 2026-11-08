// ch21-production/secure/src/androidMain/kotlin/hearthseed/secure/AesGcmBox.kt
package hearthseed.secure

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64

/** Seals text with AES-256-GCM: Base64 of IV followed by ciphertext and tag. */
class AesGcmBox(private val key: () -> SecretKey) {
    fun seal(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key()) // the provider picks a fresh IV
        val iv = cipher.iv
        check(iv.size == IV_BYTES) { "Unexpected IV size ${iv.size}" }
        return Base64.encode(iv + cipher.doFinal(plain.encodeToByteArray()))
    }

    /** Throws if the data was tampered with or sealed with another key. */
    fun open(sealed: String): String {
        val bytes = Base64.decode(sealed)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES)
        cipher.init(Cipher.DECRYPT_MODE, key(), spec)
        val plain = cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES)
        return plain.decodeToString()
    }

    private companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}
