// mendfield/core/domain/src/commonTest/kotlin/mendfield/core/domain/QueuePlannerTest.kt
package mendfield.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import mendfield.core.domain.Fixtures.at
import mendfield.core.domain.Fixtures.callum
import mendfield.core.domain.Fixtures.grid
import mendfield.core.domain.Fixtures.ines
import mendfield.core.domain.Fixtures.item
import mendfield.core.domain.Fixtures.maud
import mendfield.core.domain.Fixtures.tomasz
import mendfield.core.model.Item
import mendfield.core.model.ItemCategory
import mendfield.core.model.RepairSlot

class QueuePlannerTest {
    private val fixers = listOf(maud, tomasz, ines, callum)
    private val slots = fixers.flatMap { grid(it) }

    private fun QueuePlan.slotOf(itemId: String) =
        slots.first { s -> s.id == assignments.first { it.itemId == itemId }.slotId }

    /** Applies the plan one assignment at a time, checking each with the rules. */
    private fun applyChecked(plan: QueuePlan, items: List<Item>): List<RepairSlot> =
        plan.assignments.fold(slots) { current, a ->
            val slot = current.first { it.id == a.slotId }
            val item = items.first { it.id == a.itemId }
            assertNull(checkAssignment(slot, item, current, items), "$a")
            current.map { if (it.id == slot.id) it.copy(itemId = item.id) else it }
        }

    @Test fun firstRegisteredIsServedFirst() {
        val late = item("late", ItemCategory.CLOTHING, registeredMinute = 20)
        val early = item("early", ItemCategory.CLOTHING, registeredMinute = 5)
        val plan = planQueue(listOf(late, early), fixers, slots, notBefore = at(10))
        assertEquals(listOf("early", "late"), plan.assignments.map { it.itemId })
        assertEquals(at(10, 0), plan.slotOf("early").start)
        assertEquals(at(10, 30), plan.slotOf("late").start)
    }

    @Test fun theLoadIsSpreadAcrossQualifiedFixers() {
        val lamps = (1..4).map { item("lamp$it", ItemCategory.LAMP, registeredMinute = it) }
        val plan = planQueue(lamps, fixers, slots, notBefore = at(10))
        assertEquals(listOf("fx-1", "fx-4", "fx-1", "fx-4"), plan.assignments.map { it.fixerId })
    }

    @Test fun slotsBeforeNotBeforeAreSkipped() {
        val coat = item("coat", ItemCategory.CLOTHING)
        val plan = planQueue(listOf(coat), fixers, slots, notBefore = at(11, 15))
        assertEquals(at(11, 30), plan.slotOf("coat").start)
    }

    @Test fun itemsWithoutAQualifiedFixerOrSlotAreUnplaced() {
        val chair = item("chair", ItemCategory.FURNITURE) // nobody does woodwork
        val coats = (1..7).map { item("coat$it", ItemCategory.CLOTHING, registeredMinute = it) }
        val plan = planQueue(listOf(chair) + coats, fixers, slots, notBefore = at(10))
        assertEquals(6, plan.assignments.size) // Ines has six slots
        assertEquals(listOf("chair", "coat7"), plan.unplaced.map { it.id })
    }

    @Test fun aVisitorWithTwoItemsGetsTwoDifferentTimes() {
        val lamp = item("lamp", ItemCategory.LAMP, visitor = "Rosa", registeredMinute = 1)
        val coat = item("coat", ItemCategory.CLOTHING, visitor = "Rosa", registeredMinute = 2)
        val plan = planQueue(listOf(lamp, coat), fixers, slots, notBefore = at(10))
        assertEquals(at(10, 0), plan.slotOf("lamp").start)
        assertEquals(at(10, 30), plan.slotOf("coat").start)
    }

    @Test fun scheduledItemsStayWhereTheyAre() {
        val lamp = item("lamp", ItemCategory.LAMP)
        val booked = slots.map { if (it.id == "evt-t.fx-4.3") it.copy(itemId = "lamp") else it }
        val plan = planQueue(listOf(lamp), fixers, booked, notBefore = at(10))
        assertEquals(QueuePlan(emptyList(), emptyList()), plan)
    }

    @Test fun aBusyQueueProducesAValidSchedule() {
        val categories = ItemCategory.entries
        val items = (0 until 30).map { n ->
            item("i$n", categories[n % categories.size],
                visitor = "Visitor ${n % 7}", registeredMinute = n)
        }
        val plan = planQueue(items, fixers, slots, notBefore = at(10))
        val result = applyChecked(plan, items)
        assertEquals(plan.assignments.size, result.count { !it.isOpen })
        assertEquals(items.size, plan.assignments.size + plan.unplaced.size)
        assertTrue(plan.unplaced.any { it.category == ItemCategory.FURNITURE })
    }
}
