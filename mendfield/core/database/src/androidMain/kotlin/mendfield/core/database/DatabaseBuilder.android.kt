// mendfield/core/database/src/androidMain/kotlin/mendfield/core/database/DatabaseBuilder.android.kt
package mendfield.core.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

/** The app's private databases folder (/data/data/<app>/databases). */
fun mendfieldDatabase(context: Context): MendfieldDatabase {
    val appContext = context.applicationContext
    val file = appContext.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<MendfieldDatabase>(appContext, file.path)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO) // never the main thread
        .build()
}
