// hearthseed/core/database/src/commonMain/kotlin/hearthseed/core/database/ExerciseDao.kt
package hearthseed.core.database

import androidx.room3.Dao
import androidx.room3.MapColumn
import androidx.room3.Query
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

/** Solutions of the exercises of Chapter 14 (tested in bundledTest). */
@Dao
interface ExerciseDao {
    // Exercise 14.1: the converter stores month m as bit m.
    @Query(
        "SELECT * FROM variety WHERE (sowing_mask & (1 << :month)) != 0 " +
            "ORDER BY common_name",
    )
    suspend fun sowableIn(month: Int): List<VarietyEntity>

    // Exercise 14.2: a Flow query emits now and after every write to variety.
    @Query("SELECT COUNT(*) FROM variety")
    fun observeVarietyCount(): Flow<Int>

    // Exercise 14.3: a multimap, one entry per row of the result.
    @Query(
        """
        SELECT v.family AS family, SUM(l.grams) AS grams
        FROM seed_lot AS l JOIN variety AS v ON v.id = l.variety_id
        WHERE l.status = 'AVAILABLE'
        GROUP BY v.family ORDER BY v.family
        """,
    )
    suspend fun availableGramsByFamily():
        Map<@MapColumn("family") String, @MapColumn("grams") Int>

    // Exercise 14.4: the foreign key's ON DELETE CASCADE removes the lots.
    @Query("DELETE FROM lot_refresh WHERE refreshed_at < :cutoff")
    suspend fun forgetLotsRefreshedBefore(cutoff: Instant): Int
}
