// mendfield/core/data/src/commonTest/kotlin/mendfield/core/data/RebaseTest.kt
package mendfield.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import mendfield.core.model.Outcome
import mendfield.core.model.OutcomeKind
import mendfield.core.sync.Operation
import mendfield.core.testing.SampleData

class RebaseTest {
    private val server = SampleData.novemberData
    private val newItem = SampleData.jacket.copy(id = "local-1",
        registeredAt = SampleData.at(-55))

    @Test fun unsentChangesAreReplayedOnTheServerCopy() {
        val outcome = Outcome("o-1", "local-1", OutcomeKind.ADVICE, "", SampleData.at(5))
        val rebased = server.withPending("evt-02", listOf(
            Operation.RegisterItem(newItem),
            Operation.AssignSlot("evt-02.fx-03.1", "local-1"),
            Operation.LogOutcome(outcome),
        ))
        // In registration order: lamp (-60), local-1 (-55), jacket (-50).
        assertEquals(listOf("item-001", "local-1", "item-002"),
            rebased.items.map { it.id })
        assertEquals("local-1", rebased.slots.first { it.id == "evt-02.fx-03.1" }.itemId)
        assertEquals(listOf(outcome), rebased.outcomes)
    }

    @Test fun otherEventsAndUnknownTargetsChangeNothing() {
        val rebased = server.withPending("evt-02", listOf(
            Operation.RegisterItem(newItem.copy(eventId = "evt-01")),
            Operation.AssignSlot("evt-01.fx-01.2", "item-002"),
            Operation.LogOutcome(Outcome("o-2", "item-999", OutcomeKind.FIXED, "",
                SampleData.at(0))),
        ))
        assertEquals(server, rebased)
    }

    @Test fun replayingTwiceGivesTheSameBoard() {
        val ops = listOf(Operation.RegisterItem(newItem))
        val once = server.withPending("evt-02", ops)
        assertEquals(once, once.withPending("evt-02", ops))
    }
}
