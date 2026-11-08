// mendfield/core/data/src/commonTest/kotlin/mendfield/core/data/SyncCoordinatorTest.kt
package mendfield.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import mendfield.core.model.ItemCategory
import mendfield.core.sync.SyncState
import mendfield.core.testing.SampleData
import mendfield.core.testing.TestCafe

class SyncCoordinatorTest {
    @Test fun offlineWorkIsDeliveredOnReconnection() = runTest {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.setOnline(false)
        cafe.sync.start(backgroundScope)
        val item = (cafe.intake.registerItem("evt-02", ItemCategory.AUDIO,
            "Speaker crackles", "Kofi Asante") as WriteResult.Saved).value
        cafe.intake.assign("evt-02.fx-01.3", item.id)
        runCurrent()
        assertEquals(SyncStatus(false, 2, emptyList(), SyncState.Idle),
            cafe.sync.status.first())

        cafe.setOnline(true)
        runCurrent()
        assertEquals(0, cafe.sync.status.first().pending)
        val serverSlot = cafe.server.data.getValue("evt-02").slots
            .first { it.id == "evt-02.fx-01.3" }
        assertEquals(item.id, serverSlot.itemId)
    }

    @Test fun aRefusedAssignmentShowsTheServersTruthAgain() = runTest {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.setOnline(false)
        cafe.sync.start(backgroundScope)
        // While this device is offline, another one takes Ines's first slot.
        val theirs = SampleData.jacket.copy(id = "item-077", visitorName = "Bo")
        cafe.server.registerDirectly(theirs)
        cafe.server.assignDirectly("evt-02.fx-03.1", theirs.id)
        cafe.intake.assign("evt-02.fx-03.1", SampleData.jacket.id)

        cafe.setOnline(true)
        runCurrent()
        val status = cafe.sync.status.first()
        assertEquals("slot_taken", status.rejected.single().code)
        val slot = cafe.events.board("evt-02").first()!!.slots
            .first { it.id == "evt-02.fx-03.1" }
        assertEquals(theirs.id, slot.itemId) // refreshed: the server's truth

        cafe.sync.dismiss(status.rejected.single().pending.id)
        assertTrue(cafe.sync.status.first().rejected.isEmpty())
    }

    @Test fun reconnectingRefreshesTheEventList() = runTest {
        val cafe = TestCafe(online = false)
        cafe.sync.start(backgroundScope)
        runCurrent()
        assertEquals(emptyList(), cafe.events.events.first())
        cafe.setOnline(true)
        runCurrent()
        assertEquals(SampleData.events, cafe.events.events.first())
    }
}
