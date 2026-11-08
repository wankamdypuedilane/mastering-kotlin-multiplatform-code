// mendfield/core/domain/src/commonTest/kotlin/mendfield/core/domain/SlotGridTest.kt
package mendfield.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalTime
import mendfield.core.domain.Fixtures.at
import mendfield.core.domain.Fixtures.event

class SlotGridTest {
    @Test fun fillsTheEventWithBackToBackSlots() {
        val slots = slotGrid(event, "fx-1", 30.minutes)
        assertEquals(6, slots.size)
        assertEquals(at(10, 0), slots.first().start)
        assertEquals(at(13, 0), slots.last().end)
        slots.zipWithNext { a, b -> assertEquals(a.end, b.start) }
        assertTrue(slots.all { it.isOpen && it.fixerId == "fx-1" })
    }

    @Test fun idsAreStableAndNumberedFromOne() {
        val ids = slotGrid(event, "fx-1", 1.hours()).map { it.id }
        assertEquals(listOf("evt-t.fx-1.1", "evt-t.fx-1.2", "evt-t.fx-1.3"), ids)
        assertEquals(ids, slotGrid(event, "fx-1", 1.hours()).map { it.id })
    }

    @Test fun aShortRemainderIsLeftFree() {
        val slots = slotGrid(event, "fx-1", 40.minutes) // 3 h = 4 x 40 min + 20 min
        assertEquals(4, slots.size)
        assertEquals(at(12, 40), slots.last().end)
    }

    @Test fun availabilityIsClippedToTheEventHours() {
        val slots = slotGrid(event, "fx-1", 30.minutes,
            from = LocalTime(11, 0), until = LocalTime(15, 0))
        assertEquals(4, slots.size)
        assertEquals(at(11, 0), slots.first().start)
        assertEquals(at(13, 0), slots.last().end)
    }

    @Test fun noSlotsWhenTheFixerIsAvailableOnlyOutsideTheEvent() {
        val slots = slotGrid(event, "fx-1", 30.minutes,
            from = LocalTime(14, 0), until = LocalTime(16, 0))
        assertEquals(emptyList(), slots)
    }

    @Test fun theLengthMustBePositive() {
        assertFailsWith<IllegalArgumentException> { slotGrid(event, "fx-1", Duration.ZERO) }
        assertFailsWith<IllegalArgumentException> { slotGrid(event, "fx-1", (-5).minutes) }
    }

    @Test fun everyGridSlotPassesTheSlotRules() {
        val slots = slotGrid(event, "fx-1", 25.minutes)
        slots.forEach { assertNull(checkNewSlot(event, slots, it)) }
    }

    private fun Int.hours() = (this * 60).minutes
}
