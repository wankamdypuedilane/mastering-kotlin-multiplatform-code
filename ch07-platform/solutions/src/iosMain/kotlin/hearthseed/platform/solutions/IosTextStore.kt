package hearthseed.platform.solutions

import hearthseed.platform.joinPath
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosTextStore(private val directory: String) : TextStore {
    override fun write(name: String, text: String) {
        NSFileManager.defaultManager.createDirectoryAtPath(
            directory, withIntermediateDirectories = true,
            attributes = null, error = null,
        )
        val written = NSString.create(string = text).writeToFile(
            joinPath(directory, name), atomically = true,
            encoding = NSUTF8StringEncoding, error = null,
        )
        check(written) { "Could not write $name" }
    }

    override fun read(name: String): String? =
        NSString.stringWithContentsOfFile(
            joinPath(directory, name), encoding = NSUTF8StringEncoding,
            error = null,
        )
}
