// mendfield/core/database/src/commonMain/kotlin/mendfield/core/database/Converters.kt
package mendfield.core.database

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant
import mendfield.core.model.Skill

/** Types SQLite does not have, as types it has. */
object Converters {
    private const val NANOS = 1_000_000_000L

    /**
     * Nanoseconds since 1970 in one INTEGER: lossless (the server sends
     * nanoseconds) and sortable in SQL, for the years 1677 to 2262.
     */
    @ColumnTypeConverter
    fun instantToNanos(instant: Instant): Long =
        instant.epochSeconds * NANOS + instant.nanosecondsOfSecond

    @ColumnTypeConverter
    fun nanosToInstant(nanos: Long): Instant = Instant.fromEpochSeconds(
        nanos.floorDiv(NANOS), nanos.mod(NANOS).toInt())

    @ColumnTypeConverter
    fun skillsToText(skills: Set<Skill>): String =
        skills.sorted().joinToString(",") { it.name }

    /** Unknown names (an older app reading newer data) are skipped. */
    @ColumnTypeConverter
    fun textToSkills(text: String): Set<Skill> = text.split(',')
        .mapNotNull { name -> Skill.entries.firstOrNull { it.name == name } }
        .toSet()
}
