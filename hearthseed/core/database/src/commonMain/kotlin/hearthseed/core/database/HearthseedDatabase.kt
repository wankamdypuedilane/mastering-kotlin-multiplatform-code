// hearthseed/core/database/src/commonMain/kotlin/hearthseed/core/database/HearthseedDatabase.kt
package hearthseed.core.database

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

@Database(
    entities = [VarietyEntity::class, LotRefreshEntity::class,
        SeedLotEntity::class],
    version = 3, // 2 -> 3 is the manual MIGRATION_2_3
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(HearthseedDatabaseConstructor::class)
abstract class HearthseedDatabase : RoomDatabase() {
    abstract fun varietyDao(): VarietyDao
    abstract fun lotDao(): LotDao
    abstract fun exerciseDao(): ExerciseDao // solutions of Chapter 14
}

// Room's compiler generates the actual object for every target.
@Suppress("KotlinNoActualForExpect")
expect object HearthseedDatabaseConstructor :
    RoomDatabaseConstructor<HearthseedDatabase> {
    override fun initialize(): HearthseedDatabase
}

/** The configuration every platform shares: migrations, then build. */
internal fun RoomDatabase.Builder<HearthseedDatabase>.configure() =
    addMigrations(*ALL_MIGRATIONS).build()

/** The file name on every platform. */
const val DATABASE_NAME = "hearthseed.db"
