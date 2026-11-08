// hearthseed/core/database/src/commonMain/kotlin/hearthseed/core/database/Converters.kt
package hearthseed.core.database

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant

/** Types SQLite does not have, as types it has. */
object Converters {
    @ColumnTypeConverter
    fun instantToMillis(instant: Instant): Long = instant.toEpochMilliseconds()

    @ColumnTypeConverter
    fun millisToInstant(millis: Long): Instant =
        Instant.fromEpochMilliseconds(millis)

    /** Month m (1..12) is bit m: SQL can test it with `mask & (1 << m)`. */
    @ColumnTypeConverter
    fun monthsToMask(months: Set<Int>): Int =
        months.fold(0) { mask, month -> mask or (1 shl month) }

    @ColumnTypeConverter
    fun maskToMonths(mask: Int): Set<Int> =
        (1..12).filter { mask and (1 shl it) != 0 }.toSet()
}
