// hearthseed/core/database/src/bundledTest/kotlin/hearthseed/core/database/Chapter14SolutionsTest.kt
package hearthseed.core.database

import hearthseed.core.model.LotStatus
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.test.runTest

class Chapter14SolutionsTest {
    private val database = inMemoryDatabase()
    private val exercises = database.exerciseDao()

    @AfterTest fun closeDatabase() = database.close()

    /** A clock that the test moves by hand. */
    private class TestClock(var now: Instant) : Clock {
        override fun now() = now
    }

    @Test fun exercise1_sowableInAMonth() = runTest {
        RoomVarietyLocalSource(database).saveVarieties(listOf(
            variety("var-1", "Ember pepper", "Solanaceae", setOf(2, 3)),
            variety("var-2", "Beet", "Amaranthaceae", setOf(3, 4, 5)),
            variety("var-3", "Kale", "Brassicaceae", setOf(7, 8)),
        ))
        assertEquals(listOf("Beet", "Ember pepper"),
            exercises.sowableIn(3).map { it.commonName })
        assertEquals(emptyList(), exercises.sowableIn(12))
    }

    @Test fun exercise2_countFollowsWrites() = runTest {
        val counts = exercises.observeVarietyCount().produceIn(backgroundScope)
        assertEquals(0, counts.receive())
        RoomVarietyLocalSource(database).saveVarieties(listOf(
            variety("var-1", "Ember pepper", "Solanaceae"),
            variety("var-2", "Beet", "Amaranthaceae"),
        ))
        assertEquals(2, counts.receive())
    }

    @Test fun exercise3_availableGramsByFamily() = runTest {
        val local = RoomVarietyLocalSource(database)
        local.saveVarieties(listOf(
            variety("var-1", "Ember pepper", "Solanaceae"),
            variety("var-2", "Gold tomato", "Solanaceae"),
            variety("var-3", "Beet", "Amaranthaceae"),
        ))
        local.saveLots("var-1", listOf(lot("lot-1", "var-1", grams = 12),
            lot("lot-2", "var-1", grams = 30, status = LotStatus.ON_LOAN)))
        local.saveLots("var-2", listOf(lot("lot-3", "var-2", grams = 8)))
        local.saveLots("var-3", listOf(lot("lot-4", "var-3", grams = 25)))
        assertEquals(mapOf("Amaranthaceae" to 25, "Solanaceae" to 20),
            exercises.availableGramsByFamily())
    }

    @Test fun exercise4_forgettingOldRefreshesCascadesToLots() = runTest {
        val clock = TestClock(Instant.parse("2026-11-01T09:00:00Z"))
        val local = RoomVarietyLocalSource(database, clock)
        local.saveLots("var-1", listOf(lot("lot-1", "var-1")))
        clock.now += 10.days
        local.saveLots("var-2", listOf(lot("lot-2", "var-2")))
        val oldLots = local.observeLots("var-1").produceIn(backgroundScope)
        assertEquals(1, oldLots.receive()?.size)

        val forgotten = exercises.forgetLotsRefreshedBefore(clock.now - 7.days)

        assertEquals(1, forgotten)
        assertNull(oldLots.receive())   // observers see "never loaded" again
        assertNull(local.lot("lot-1"))  // removed by ON DELETE CASCADE
        assertEquals(1, local.observeLots("var-2").first()?.size)
    }
}
