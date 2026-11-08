// ch21-production/secure/src/androidMain/kotlin/hearthseed/secure/KeystoreSecureStore.kt
package hearthseed.secure

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

actual val secureStorageKind: SecureStorageKind =
    SecureStorageKind.ANDROID_KEYSTORE

/**
 * Ciphertext in a private SharedPreferences file; the AES key lives in the
 * Android Keystore and never enters the app process.
 */
class KeystoreSecureStore(
    context: Context,
    private val keyAlias: String = "hearthseed.secure.v1",
) : SecureStore {
    private val prefs =
        context.getSharedPreferences("hearthseed.secure", Context.MODE_PRIVATE)
    private val box = AesGcmBox(::secretKey)

    // Keystore calls can be slow: keep them off the main thread.
    override suspend fun read(key: String): String? = withContext(IO) {
        val sealed = prefs.getString(key, null) ?: return@withContext null
        try {
            box.open(sealed)
        } catch (e: GeneralSecurityException) {
            // Key gone (restored backup, new device...): forget the value.
            prefs.edit().remove(key).apply()
            null
        }
    }

    override suspend fun write(key: String, value: String) = withContext(IO) {
        prefs.edit().putString(key, box.seal(value)).apply()
    }

    override suspend fun delete(key: String) = withContext(IO) {
        prefs.edit().remove(key).apply()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }
        (keyStore.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        val generator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        return generator.apply { init(spec) }.generateKey()
    }

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
    }
}
