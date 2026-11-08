// mendfield/core/database/src/jvmMain/kotlin/mendfield/core/database/DatabaseBuilder.jvm.kt
package mendfield.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlinx.coroutines.Dispatchers

/** Desktop: a file in the user's home folder (not the temp folder). */
fun defaultDatabaseFile(): File =
    File(System.getProperty("user.home"), ".mendfield/$DATABASE_NAME")

fun mendfieldDatabase(file: File = defaultDatabaseFile()): MendfieldDatabase {
    file.parentFile?.mkdirs()
    return Room.databaseBuilder<MendfieldDatabase>(name = file.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
