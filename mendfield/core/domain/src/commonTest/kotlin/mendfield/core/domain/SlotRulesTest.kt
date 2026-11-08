// mendfield/core/domain/src/commonTest/kotlin/mendfield/core/domain/SlotRulesTest.kt
package mendfield.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import mendfield.core.domain.Fixtures.at
import mendfield.core.domain.Fixtures.event
import mendfield.core.domain.Fixtures.grid
import mendfield.core.domain.Fixtures.ines
import mendfield.core.domain.Fixtures.item
import mendfield.core.domain.Fixtures.maud
import mendfield.core.model.ItemCategory
import mendfield.core.model.RepairSlot

class SlotRulesTest {
    private fun slot(id: String, fromH: Int, fromM: Int, toH: Int, toM: Int) =
        RepairSlot(id, event.id, "fx-1", at(fromH, fromM), at(toH, toM))

    @Test fun aNewSlotMustLieInsideTheEventHours() {
        assertEquals(SlotProblem.OutsideEventHours,
            checkNewSlot(event, emptyList(), slot("s", 9, 45, 10, 15)))
        assertEquals(SlotProblem.OutsideEventHours,
            checkNewSlot(event, emptyList(), slot("s", 12, 45, 13, 15)))
        assertNull(checkNewSlot(event, emptyList(), slot("s", 12, 30, 13, 0)))
    }

    @Test fun aNewSlotMustNotOverlapTheFixersOtherSlots() {
        val existing = listOf(slot("a", 10, 0, 10, 30))
        assertEquals(SlotProblem.Overlaps("a"),
            checkNewSlot(event, existing, slot("b", 10, 15, 10, 45)))
        assertNull(checkNewSlot(event, existing, slot("b", 10, 30, 11, 0)))
    }

    @Test fun otherFixersMayWorkAtTheSameTime() {
        val existing = listOf(slot("a", 10, 0, 10, 30).copy(fixerId = "fx-2"))
        assertNull(checkNewSlot(event, existing, slot("b", 10, 0, 10, 30)))
    }

    @Test fun aSlotOfAnotherEventIsRejected() {
        assertEquals(SlotProblem.WrongEvent,
            checkNewSlot(event, emptyList(), slot("s", 10, 0, 10, 30).copy(eventId = "x")))
    }

    @Test fun anOpenSlotAcceptsAnItem() {
        val slots = grid(maud)
        val lamp = item("i1", ItemCategory.LAMP)
        assertNull(checkAssignment(slots[0], lamp, slots, listOf(lamp)))
    }

    @Test fun repeatingAnAssignmentIsAllowed() {
        val lamp = item("i1", ItemCategory.LAMP)
        val slots = grid(maud).mapIndexed { i, s -> if (i == 0) s.copy(itemId = "i1") else s }
        assertNull(checkAssignment(slots[0], lamp, slots, listOf(lamp)))
    }

    @Test fun aTakenSlotIsRejected() {
        val lamp = item("i1", ItemCategory.LAMP)
        val slots = grid(maud).mapIndexed { i, s -> if (i == 0) s.copy(itemId = "i0") else s }
        assertEquals(AssignmentProblem.SlotTaken("i0"),
            checkAssignment(slots[0], lamp, slots, listOf(lamp)))
    }

    @Test fun anItemGetsOneSlotOnly() {
        val lamp = item("i1", ItemCategory.LAMP)
        val slots = grid(maud).mapIndexed { i, s -> if (i == 0) s.copy(itemId = "i1") else s }
        assertEquals(AssignmentProblem.ItemAlreadyScheduled(slots[0].id),
            checkAssignment(slots[3], lamp, slots, listOf(lamp)))
    }

    @Test fun aVisitorIsNotAtTwoTablesAtOnce() {
        val lamp = item("i1", ItemCategory.LAMP, visitor = "Rosa Lindqvist")
        val coat = item("i2", ItemCategory.CLOTHING, visitor = "  rosa lindqvist ")
        val maudSlots = grid(maud).mapIndexed { i, s -> if (i == 0) s.copy(itemId = "i1") else s }
        val inesSlots = grid(ines)
        val all = maudSlots + inesSlots
        assertEquals(AssignmentProblem.VisitorBusy(maudSlots[0].id),
            checkAssignment(inesSlots[0], coat, all, listOf(lamp, coat)))
        assertNull(checkAssignment(inesSlots[1], coat, all, listOf(lamp, coat)))
    }

    @Test fun anItemOfAnotherEventIsRejected() {
        val lamp = item("i1", ItemCategory.LAMP).copy(eventId = "other")
        assertEquals(AssignmentProblem.WrongEvent,
            checkAssignment(grid(maud)[0], lamp, grid(maud), listOf(lamp)))
    }
}
