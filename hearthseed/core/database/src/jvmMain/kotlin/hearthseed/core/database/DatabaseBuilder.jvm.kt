// hearthseed/core/database/src/jvmMain/kotlin/hearthseed/core/database/DatabaseBuilder.jvm.kt
package hearthseed.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlinx.coroutines.Dispatchers

/** Desktop: a file in the user's home folder (not the temp folder). */
fun defaultDatabaseFile(): File =
    File(System.getProperty("user.home"), ".hearthseed/$DATABASE_NAME")

fun hearthseedDatabase(file: File = defaultDatabaseFile()): HearthseedDatabase {
    file.parentFile?.mkdirs()
    return Room.databaseBuilder<HearthseedDatabase>(name = file.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .configure()
}
