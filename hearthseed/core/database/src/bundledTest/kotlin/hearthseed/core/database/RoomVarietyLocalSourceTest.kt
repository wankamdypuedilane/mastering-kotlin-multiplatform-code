// hearthseed/core/database/src/bundledTest/kotlin/hearthseed/core/database/RoomVarietyLocalSourceTest.kt
package hearthseed.core.database

import hearthseed.core.model.LotStatus
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.test.runTest

class RoomVarietyLocalSourceTest {
    private val database = inMemoryDatabase()
    private val local = RoomVarietyLocalSource(database)

    @AfterTest fun closeDatabase() = database.close()

    @Test fun searchMatchesNamesIgnoringCaseAndFiltersByFamily() = runTest {
        local.saveVarieties(listOf(
            variety("var-2", "Quarry Gold tomato", "Solanaceae"),
            variety("var-1", "Larkmoor Ember tomato", "Solanaceae"),
            variety("var-3", "Larkmoor Pea", "Fabaceae"),
        ))
        assertEquals(listOf("var-1", "var-2"),
            local.search("TOMATO", null).map { it.id })
        assertEquals(listOf("var-3"),
            local.search("larkmoor", "fabaceae").map { it.id })
    }

    @Test fun aVarietyComesBackAsItWasSaved() = runTest {
        val pea = variety("var-3", "Larkmoor Pea", "Fabaceae", setOf(3, 4, 10))
        local.saveVarieties(listOf(pea))
        local.saveVarieties(listOf(pea.copy(daysToMaturity = 70))) // upsert
        assertEquals(pea.copy(daysToMaturity = 70), local.variety("var-3"))
    }

    @Test fun lotsAreNullUntilLoadedThenFollowEveryWrite() = runTest {
        // A channel receives each emission of the Room Flow, in order.
        val lots = local.observeLots("var-3").produceIn(backgroundScope)
        assertNull(lots.receive()) // never loaded

        local.saveLots("var-3",
            listOf(lot("lot-2", "var-3"), lot("lot-1", "var-3")))
        assertEquals(listOf("lot-1", "lot-2"), lots.receive()?.map { it.id })

        local.updateLotStatus("lot-1", LotStatus.ON_LOAN) // e.g. after a borrow
        assertEquals(LotStatus.ON_LOAN, lots.receive()?.first()?.status)

        local.saveLots("var-3", emptyList()) // loaded, and no lots left
        assertEquals(emptyList(), lots.receive())
    }

    @Test fun aRefreshReplacesTheOldLots() = runTest {
        local.saveLots("var-3",
            listOf(lot("lot-1", "var-3"), lot("lot-2", "var-3")))
        local.saveLots("var-3", listOf(lot("lot-2", "var-3", grams = 4)))
        assertNull(local.lot("lot-1"))
        assertEquals(4, local.lot("lot-2")?.grams)
    }
}
