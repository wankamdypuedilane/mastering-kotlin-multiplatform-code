// mendfield/core/database/src/commonMain/kotlin/mendfield/core/database/MendfieldDatabase.kt
package mendfield.core.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

/** The cache and the outbox in one file: one transaction can span both. */
@Database(
    entities = [EventEntity::class, FixerEntity::class, ItemEntity::class,
        SlotEntity::class, OutcomeEntity::class, OutboxEntity::class],
    version = 1, // schemas/.../1.json; a change needs a migration
)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(MendfieldDatabaseConstructor::class)
abstract class MendfieldDatabase : RoomDatabase() {
    abstract fun cafeDao(): CafeDao
    abstract fun outboxDao(): OutboxDao
}

// Room's compiler generates the actual object for every target.
@Suppress("KotlinNoActualForExpect")
expect object MendfieldDatabaseConstructor :
    RoomDatabaseConstructor<MendfieldDatabase> {
    override fun initialize(): MendfieldDatabase
}

/** The file name on every platform. */
const val DATABASE_NAME = "mendfield.db"
