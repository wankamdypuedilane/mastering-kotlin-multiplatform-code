// mendfield/solutions/src/commonTest/kotlin/mendfield/solutions/SolutionsTest.kt
package mendfield.solutions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import mendfield.core.data.EventBoard
import mendfield.core.data.WriteResult
import mendfield.core.model.ItemCategory
import mendfield.core.model.OutcomeKind
import mendfield.core.testing.SampleData
import mendfield.core.testing.TestCafe

@OptIn(ExperimentalCoroutinesApi::class) // runCurrent
class SolutionsTest {
    private val board = EventBoard(SampleData.november, SampleData.fixers,
        SampleData.novemberData)

    @Test fun freeCapacityCountsOpenSlotsMostFreeFirst() {
        // At 10:00 Maud has given her first slot to the lamp.
        assertEquals(
            listOf(FixerCapacity("fx-03", "Ines Halloran", 4),
                FixerCapacity("fx-02", "Tomasz Wren", 4),
                FixerCapacity("fx-01", "Maud Okafor", 3)),
            board.freeCapacity(SampleData.at(0)),
        )
        // From 11:00 only the last two slots of each fixer count.
        assertEquals(listOf(2, 2, 2),
            board.freeCapacity(SampleData.at(60)).map { it.free })
    }

    @Test fun pendingChangesAreCountedPerEvent() = runTest {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.setOnline(false)
        val badges = pendingByEvent(cafe.store, cafe.cache)
        assertEquals(emptyMap(), badges.first())

        val item = (cafe.intake.registerItem("evt-02", ItemCategory.AUDIO,
            "Speaker crackles", "Lin Okoro") as WriteResult.Saved).value
        cafe.intake.assign("evt-02.fx-01.2", item.id)
        cafe.outcomes.logOutcome(SampleData.toaster.id, OutcomeKind.PARTIAL, "")
        assertEquals(mapOf("evt-02" to 2, "evt-01" to 1), badges.first())

        cafe.setOnline(true)
        cafe.engine.syncNow()
        assertEquals(emptyMap(), badges.first())
    }

    @Test fun aLostSlotGetsNewProposalsAndCanBeRescheduled() = runTest {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.setOnline(false)
        cafe.sync.start(backgroundScope)
        // Offline, this device gives Ines's first slot to the jacket; another
        // organizer's device gives the same slot to another item first.
        val theirs = SampleData.jacket.copy(id = "item-077", visitorName = "Bo")
        cafe.server.registerDirectly(theirs)
        cafe.server.assignDirectly("evt-02.fx-03.1", theirs.id)
        cafe.intake.assign("evt-02.fx-03.1", SampleData.jacket.id)
        cafe.setOnline(true)
        runCurrent() // sync, refusal, refresh of evt-02

        val resolver = ConflictResolver(cafe.events, cafe.intake, cafe.store,
            cafe.clock)
        val refusal = cafe.sync.status.first().rejected.single()
        assertEquals("slot_taken", refusal.code)
        val proposals = resolver.proposals(refusal)
        // Ines is the only sewing fixer: her next open slot, at 10:30.
        assertEquals(listOf("evt-02.fx-03.2"), proposals.map { it.slot.id })

        val result = resolver.accept(refusal, proposals.first().slot.id)
        assertTrue(result is WriteResult.Saved)
        runCurrent() // the new assignment is sent at once
        val status = cafe.sync.status.first()
        assertEquals(0, status.pending)
        assertTrue(status.rejected.isEmpty())
        val serverSlot = cafe.server.data.getValue("evt-02").slots
            .single { it.id == "evt-02.fx-03.2" }
        assertEquals(SampleData.jacket.id, serverSlot.itemId)
        // Once the item has a slot, there is nothing left to propose.
        assertEquals(emptyList(), resolver.proposals(refusal))
    }
}
