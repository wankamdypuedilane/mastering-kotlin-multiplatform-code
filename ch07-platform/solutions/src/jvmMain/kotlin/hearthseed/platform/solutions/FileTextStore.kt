package hearthseed.platform.solutions

import java.io.File

class FileTextStore(private val directory: String) : TextStore {
    override fun write(name: String, text: String) {
        File(directory).mkdirs()
        File(directory, name).writeText(text)
    }

    override fun read(name: String): String? =
        File(directory, name).takeIf { it.isFile }?.readText()
}
