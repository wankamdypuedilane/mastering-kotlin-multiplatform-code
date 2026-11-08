// hearthseed/core/database/src/iosMain/kotlin/hearthseed/core/database/DatabaseBuilder.ios.kt
package hearthseed.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** The app's Documents folder, inside its sandbox. */
fun hearthseedDatabase(): HearthseedDatabase =
    Room.databaseBuilder<HearthseedDatabase>(
        name = "${documentsPath()}/$DATABASE_NAME",
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .configure()

@OptIn(ExperimentalForeignApi::class)
private fun documentsPath(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path) { "No Documents folder" }
}
