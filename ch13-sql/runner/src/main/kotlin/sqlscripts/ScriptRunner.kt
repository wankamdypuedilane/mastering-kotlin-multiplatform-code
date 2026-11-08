// ch13-sql/runner/src/main/kotlin/sqlscripts/ScriptRunner.kt
package sqlscripts

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/** One SQL statement and the line of the script where it starts. */
data class SqlStatement(val sql: String, val line: Int)

/**
 * Splits a script into statements at every `;` outside a string literal or
 * a `--` comment. Enough for the scripts of this chapter (no triggers).
 */
fun splitStatements(script: String): List<SqlStatement> {
    val result = mutableListOf<SqlStatement>()
    val current = StringBuilder()
    var line = 1
    var startLine = 0
    var inString = false
    var inComment = false
    for (ch in script) {
        when {
            inComment -> if (ch == '\n') {
                inComment = false
                current.append(ch)
            }
            inString -> {
                current.append(ch)
                if (ch == '\'') inString = false
            }
            ch == '-' && current.endsWith("-") -> {
                current.setLength(current.length - 1)
                inComment = true
            }
            ch == ';' -> {
                val sql = current.toString().trim()
                if (sql.isNotEmpty()) result += SqlStatement(sql, startLine)
                current.setLength(0)
            }
            else -> {
                if (current.isBlank() && !ch.isWhitespace()) startLine = line
                if (ch == '\'') inString = true
                current.append(ch)
            }
        }
        if (ch == '\n') line++
    }
    check(current.isBlank()) { "statement without ';' at line $startLine" }
    return result
}

/** Renders rows like `sqlite3 -header -column -nullvalue NULL` (3.53+). */
fun renderColumns(header: List<String>, rows: List<List<Any?>>): List<String> {
    val cells = rows.map { row -> row.map(::formatValue) }
    val widths = header.indices.map { c ->
        maxOf(header[c].length, cells.maxOfOrNull { it[c].length } ?: 0)
    }
    fun join(parts: List<String>) = parts.joinToString("  ").trimEnd()
    val out = mutableListOf<String>()
    out += join(header.mapIndexed { c, title ->
        val left = (widths[c] - title.length) / 2
        (" ".repeat(left) + title).padEnd(widths[c])
    })
    out += join(widths.map { "-".repeat(it) })
    rows.forEachIndexed { r, row ->
        out += join(row.mapIndexed { c, value ->
            val text = cells[r][c]
            if (value is Number) text.padStart(widths[c])
            else text.padEnd(widths[c])
        })
    }
    return out
}

private fun formatValue(value: Any?): String = when (value) {
    null -> "NULL"
    is Double ->
        if (value % 1.0 == 0.0) "%.1f".format(value) else value.toString()
    is ByteArray -> value.joinToString("") { "%02X".format(it) }
    else -> value.toString()
}

/** One row of EXPLAIN QUERY PLAN: its id, the id of its parent, the text. */
data class PlanRow(val id: Int, val parent: Int, val detail: String)

/** Draws the plan as a tree, as the sqlite3 shell does. */
fun renderPlan(rows: List<PlanRow>): List<String> {
    val byId = rows.associateBy { it.id }
    fun isLast(index: Int): Boolean {
        val parent = rows[index].parent
        return rows.drop(index + 1).none { it.parent == parent }
    }
    val out = mutableListOf("QUERY PLAN")
    rows.forEachIndexed { index, row ->
        val prefix = StringBuilder()
        var ancestor = byId[row.parent]
        while (ancestor != null) {
            val last = isLast(rows.indexOf(ancestor))
            prefix.insert(0, if (last) "   " else "|  ")
            ancestor = byId[ancestor.parent]
        }
        out += prefix.toString() + (if (isLast(index)) "`--" else "|--") +
            row.detail
    }
    return out
}

/** Runs every statement; failures print like the shell and do not stop. */
fun runScript(connection: Connection, script: String): List<String> {
    val out = mutableListOf<String>()
    for (statement in splitStatements(script)) {
        try {
            connection.createStatement().use { st ->
                if (!st.execute(statement.sql)) return@use
                val rs = st.resultSet
                val columns = (1..rs.metaData.columnCount)
                    .map { rs.metaData.getColumnLabel(it) }
                val rows = buildList {
                    while (rs.next()) {
                        add(columns.indices.map { rs.getObject(it + 1) })
                    }
                }
                val plan = statement.sql.startsWith("EXPLAIN QUERY PLAN", true)
                out += if (plan) {
                    renderPlan(rows.map {
                        PlanRow((it[0] as Number).toInt(),
                            (it[1] as Number).toInt(), it[3] as String)
                    })
                } else {
                    renderColumns(columns, rows)
                }
            }
        } catch (e: SQLException) {
            out += "Error near line ${statement.line}: ${shellMessage(e)}"
        }
    }
    return out
}

/** sqlite-jdbc wraps the SQLite message: "[CODE] description (message)". */
private fun shellMessage(e: SQLException): String {
    val text = e.message.orEmpty()
    val open = text.indexOf(" (")
    return if (text.startsWith("[") && open > 0 && text.endsWith(")")) {
        text.substring(open + 2, text.length - 1)
    } else {
        text
    }
}

/** A new in-memory database with foreign keys enforced. */
fun openDatabase(): Connection =
    DriverManager.getConnection("jdbc:sqlite::memory:").apply {
        createStatement().use { it.execute("PRAGMA foreign_keys = ON") }
    }
