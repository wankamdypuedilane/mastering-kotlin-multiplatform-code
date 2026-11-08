// hearthseed/core/database/src/jvmTest/kotlin/hearthseed/core/database/MigrationTest.kt
package hearthseed.core.database

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.io.path.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class MigrationTest {
    private val file = createTempFile("hearthseed", ".db")
    private val helper = MigrationTestHelper(
        schemaDirectoryPath = Path("schemas"), // relative to core/database
        databasePath = file,
        driver = BundledSQLiteDriver(),
        databaseClass = HearthseedDatabase::class,
    )

    @AfterTest fun deleteFile() { file.deleteIfExists() }

    @Test fun version1DataSurvivesTheMigrationsTo3() = runTest {
        helper.createDatabase(1).apply { // the schema of 1.json
            execSQL("INSERT INTO variety VALUES ('var-3', 'Larkmoor Pea', " +
                "'Pisum sativum', 'Fabaceae', 60, '3,4,10', 'EASY', '')")
            close()
        }
        // Runs AutoMigration(1, 2) and MIGRATION_2_3, then compares the
        // resulting tables with 3.json: a missing index or column fails here.
        helper.runMigrationsAndValidate(3, ALL_MIGRATIONS.toList()).apply {
            prepare("SELECT sowing_mask FROM variety").use { statement ->
                statement.step()
                val expected = (1 shl 3) or (1 shl 4) or (1 shl 10)
                assertEquals(expected, statement.getInt(0))
            }
            close()
        }
        // And Room reads the migrated row back through the converter.
        val database = hearthseedDatabase(file.toFile())
        val pea = RoomVarietyLocalSource(database).variety("var-3")
        assertEquals(setOf(3, 4, 10), pea?.sowingMonths)
        database.close()
    }

    @Test fun theDesktopDatabaseSurvivesARestart() = runTest {
        file.deleteIfExists() // hearthseedDatabase creates it
        hearthseedDatabase(file.toFile()).apply {
            RoomVarietyLocalSource(this).saveVarieties(
                listOf(variety("var-1", "Ember pepper", "Solanaceae")))
            close() // the process ends
        }
        val reopened = hearthseedDatabase(file.toFile())
        assertEquals("Ember pepper",
            RoomVarietyLocalSource(reopened).variety("var-1")?.commonName)
        reopened.close()
    }
}
