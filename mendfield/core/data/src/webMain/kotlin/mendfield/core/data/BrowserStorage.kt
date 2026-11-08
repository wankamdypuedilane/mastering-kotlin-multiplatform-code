// mendfield/core/data/src/webMain/kotlin/mendfield/core/data/BrowserStorage.kt
package mendfield.core.data

import kotlinx.browser.window
import org.w3c.dom.Storage

// webMain: one file for Kotlin/JS and Kotlin/Wasm (kotlinx-browser has both).
/** A [KeyValueStorage] over the browser's localStorage. */
class BrowserStorage(private val storage: Storage) : KeyValueStorage {
    override fun read(key: String): String? = storage.getItem(key)

    override fun write(key: String, value: String) {
        try {
            storage.setItem(key, value)
        } catch (e: Throwable) {
            // QuotaExceededError: the queue stays in memory until it fits.
        }
    }
}

/**
 * localStorage when the browser allows it; some privacy settings make the
 * property itself throw. Then the outbox lives in memory, like the cache.
 */
fun localStorageOrMemory(): KeyValueStorage = try {
    val storage = window.localStorage
    storage.setItem("mendfield.probe", "1")
    storage.removeItem("mendfield.probe")
    BrowserStorage(storage)
} catch (e: Throwable) {
    InMemoryStorage()
}
