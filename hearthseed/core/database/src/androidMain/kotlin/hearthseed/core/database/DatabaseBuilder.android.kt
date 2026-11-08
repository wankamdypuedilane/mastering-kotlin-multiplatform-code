// hearthseed/core/database/src/androidMain/kotlin/hearthseed/core/database/DatabaseBuilder.android.kt
package hearthseed.core.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

/** The app's private databases folder (/data/data/<app>/databases). */
fun hearthseedDatabase(context: Context): HearthseedDatabase {
    val appContext = context.applicationContext
    val file = appContext.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<HearthseedDatabase>(appContext, file.path)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .configure()
}
