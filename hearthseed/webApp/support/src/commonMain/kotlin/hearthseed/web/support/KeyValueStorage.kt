// hearthseed/webApp/support/src/commonMain/kotlin/hearthseed/web/support/KeyValueStorage.kt
package hearthseed.web.support

/** The part of the browser's Web Storage that the app uses. */
interface KeyValueStorage {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun remove(key: String)
}

/** For tests, and the fallback when the browser refuses localStorage. */
class InMemoryStorage(initial: Map<String, String> = emptyMap()) :
    KeyValueStorage {
    private val values = initial.toMutableMap()
    override fun read(key: String): String? = values[key]
    override fun write(key: String, value: String) { values[key] = value }
    override fun remove(key: String) { values.remove(key) }
}
