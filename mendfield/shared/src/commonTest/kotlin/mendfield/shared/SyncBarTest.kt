// mendfield/shared/src/commonTest/kotlin/mendfield/shared/SyncBarTest.kt
package mendfield.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import mendfield.core.data.SyncStatus
import mendfield.core.sync.RejectedOperation
import mendfield.core.sync.SyncState
import mendfield.core.testing.OutboxStoreContract
import mendfield.shared.sync.Tone
import mendfield.shared.sync.syncBarState

class SyncBarTest {
    private fun text(status: SyncStatus) = syncBarState(status).text

    @Test fun everySituationHasItsSentence() {
        val idle = SyncStatus(online = true, pending = 0, rejected = emptyList(),
            state = SyncState.Idle)
        assertEquals("All changes sent", text(idle))
        assertEquals(Tone.CALM, syncBarState(idle).tone)
        assertEquals("1 change waiting", text(idle.copy(pending = 1)))
        assertEquals("Offline · 3 changes waiting",
            text(idle.copy(online = false, pending = 3)))
        assertEquals("Sending 2 changes…",
            text(idle.copy(pending = 2, state = SyncState.Syncing)))
        assertEquals("No answer, retrying in 4 s · 2 changes waiting",
            text(idle.copy(pending = 2, state = SyncState.WaitingToRetry(3, 4.seconds))))
        assertEquals("Working offline · 2 changes waiting",
            text(idle.copy(pending = 2, online = false, workingOffline = true)))
    }

    @Test fun refusedOperationsBecomeRows() {
        val op = OutboxStoreContract.op("op-1") // an AssignSlot
        val status = SyncStatus(true, 0,
            listOf(RejectedOperation(op, "slot_taken", "The slot holds item-011")),
            SyncState.Idle)
        assertEquals(
            "Put item item-op-1 in slot evt-02.fx-01.2: The slot holds item-011 (slot_taken)",
            syncBarState(status).refused.single().text)
    }
}
