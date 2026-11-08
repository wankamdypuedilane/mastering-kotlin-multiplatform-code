package hearthseed.platform.solutions

/** Exercise 7.4: a tiny file store that common code can depend on. */
interface TextStore {
    fun write(name: String, text: String)
    fun read(name: String): String?
}

/** For the web, and as a fake in tests. */
class InMemoryTextStore : TextStore {
    private val files = mutableMapOf<String, String>()
    override fun write(name: String, text: String) { files[name] = text }
    override fun read(name: String): String? = files[name]
}
