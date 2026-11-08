// hearthseed/core/database/src/commonMain/kotlin/hearthseed/core/database/Migrations.kt
package hearthseed.core.database

import androidx.room3.migration.Migration
import androidx.sqlite.async.executeSQL

/** SQL that turns the text "3,4,5" into the bits (1 << 3) | (1 << 4) | ... */
private val textToMask = (1..12).joinToString(" | ") { month ->
    "((instr(',' || sowing_months || ',', ',$month,') > 0) << $month)"
}

/**
 * Version 3 stores the sowing months as a bit mask. Room cannot guess how to
 * convert the data, so this migration rebuilds the table by hand (the
 * 12-step procedure of Chapter 13); Room runs it inside a transaction.
 */
val MIGRATION_2_3 = Migration(2, 3) { connection ->
    connection.executeSQL(
        "CREATE TABLE `variety_new` (`id` TEXT NOT NULL, " +
            "`common_name` TEXT NOT NULL, `latin_name` TEXT NOT NULL, " +
            "`family` TEXT NOT NULL, `days_to_maturity` INTEGER NOT NULL, " +
            "`sowing_mask` INTEGER NOT NULL, `difficulty` TEXT NOT NULL, " +
            "`description` TEXT NOT NULL, PRIMARY KEY(`id`))",
    )
    connection.executeSQL(
        "INSERT INTO variety_new SELECT id, common_name, latin_name, family, " +
            "days_to_maturity, $textToMask, difficulty, description " +
            "FROM variety",
    )
    connection.executeSQL("DROP TABLE variety")
    connection.executeSQL("ALTER TABLE variety_new RENAME TO variety")
    connection.executeSQL(
        "CREATE INDEX `index_variety_family` ON `variety` (`family`)",
    )
}

/** Every manual migration, for the builders and the migration tests. */
val ALL_MIGRATIONS = arrayOf(MIGRATION_2_3)
