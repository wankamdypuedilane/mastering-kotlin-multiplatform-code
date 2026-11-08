// ch13-sql/runner/src/test/kotlin/sqlscripts/ScriptsTest.kt
package sqlscripts

import java.io.File
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ScriptsTest {
    private val dir = File(System.getProperty("sql.dir") ?: "..")
    private val schema = File(dir, "schema.sql").readText()
    private val seed = File(dir, "seed.sql").readText()

    private fun scripts(): List<File> =
        listOf("queries", "exercises/solutions").flatMap { folder ->
            File(dir, folder).listFiles { f -> f.extension == "sql" }!!
                .sortedBy { it.name }
        }

    private fun freshLibrary() = openDatabase().also { db ->
        assertEquals(emptyList(), runScript(db, schema))
        assertEquals(emptyList(), runScript(db, seed))
    }

    @Test
    fun everyScriptPrintsTheExpectedOutput() {
        val scripts = scripts()
        assertTrue(scripts.size >= 13, "found ${scripts.size} scripts")
        val failures = scripts.mapNotNull { script ->
            val name = script.nameWithoutExtension
            val expected = File(dir, "expected/$name.txt").readLines()
            val actual = freshLibrary().use { runScript(it, script.readText()) }
            if (actual == expected) {
                null
            } else {
                "${script.name}\n--- expected\n${expected.joinToString("\n")}" +
                    "\n--- actual\n${actual.joinToString("\n")}"
            }
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n\n"))
    }

    @Test
    fun foreignKeysAreEnforcedOnEveryConnection() {
        freshLibrary().use { db ->
            val e = assertFailsWith<SQLException> {
                db.createStatement().execute(
                    "INSERT INTO loan (lot_id, member_id, borrowed_on, " +
                        "due_season, grams) " +
                        "VALUES (1, 42, '2026-11-08', 2027, 5)",
                )
            }
            assertTrue("FOREIGN KEY" in e.message.orEmpty(), e.message)
        }
    }

    @Test
    fun splitterKeepsStartLinesAndIgnoresComments() {
        val script = "-- header\nSELECT 'a;b';\n\nSELECT 1 -- one\n  + 1;\n"
        assertEquals(
            listOf(
                SqlStatement("SELECT 'a;b'", 2),
                SqlStatement("SELECT 1 \n  + 1", 4),
            ),
            splitStatements(script),
        )
    }

    @Test
    fun planRendererDrawsBranches() {
        val rows = listOf(PlanRow(2, 0, "SCAN a"), PlanRow(3, 2, "INDEX"),
            PlanRow(4, 0, "SORT"))
        assertEquals(
            listOf("QUERY PLAN", "|--SCAN a", "|  `--INDEX", "`--SORT"),
            renderPlan(rows),
        )
    }

    @Test
    fun reportsTheBundledSqliteVersion() {
        val version = openDatabase().use { db ->
            db.createStatement().executeQuery("SELECT sqlite_version()")
                .use { rs -> rs.next(); rs.getString(1) }
        }
        println("SQLite $version (sqlite-jdbc)")
        assertTrue(version.startsWith("3."), version)
    }
}
