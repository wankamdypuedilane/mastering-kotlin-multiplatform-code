// hearthseed/webApp/support/src/webMain/kotlin/hearthseed/web/support/BrowserStorage.kt
package hearthseed.web.support

import kotlinx.browser.window
import org.w3c.dom.Storage

// webMain: one file for Kotlin/JS and Kotlin/Wasm (kotlinx-browser has both).
/** A [KeyValueStorage] over the browser's localStorage or sessionStorage. */
class BrowserStorage(private val storage: Storage) : KeyValueStorage {
    override fun read(key: String): String? = storage.getItem(key)

    override fun write(key: String, value: String) {
        try {
            storage.setItem(key, value)
        } catch (e: Throwable) {
            // QuotaExceededError: the page keeps working, unsaved.
        }
    }

    override fun remove(key: String) = storage.removeItem(key)
}

/**
 * localStorage when the browser allows it. It can refuse: some privacy
 * settings make the property itself throw. Then the favorites live in memory
 * until the tab closes.
 */
fun localStorageOrMemory(): KeyValueStorage = try {
    val storage = window.localStorage
    storage.setItem(PROBE_KEY, "1")
    storage.removeItem(PROBE_KEY)
    BrowserStorage(storage)
} catch (e: Throwable) {
    InMemoryStorage()
}

private const val PROBE_KEY = "hearthseed.probe"
