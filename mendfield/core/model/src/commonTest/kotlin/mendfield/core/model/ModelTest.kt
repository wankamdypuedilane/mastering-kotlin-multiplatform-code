// mendfield/core/model/src/commonTest/kotlin/mendfield/core/model/ModelTest.kt
package mendfield.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

class ModelTest {
    private val ten = Instant.parse("2026-11-21T10:00:00Z")
    private val halfPast = Instant.parse("2026-11-21T10:30:00Z")

    @Test fun anEventMustOpenBeforeItCloses() {
        assertFailsWith<IllegalArgumentException> {
            Event("e", "Late", "Hall", LocalDate(2026, 11, 21),
                LocalTime(13, 0), LocalTime(10, 0), TimeZone.UTC)
        }
    }

    @Test fun aSlotMustHaveAPositiveLength() {
        assertFailsWith<IllegalArgumentException> {
            RepairSlot("s", "e", "f", start = ten, end = ten)
        }
    }

    @Test fun aSlotIsOpenUntilItHoldsAnItem() {
        val slot = RepairSlot("s", "e", "f", ten, halfPast)
        assertTrue(slot.isOpen)
        assertFalse(slot.copy(itemId = "item-1").isOpen)
    }

    @Test fun everySkillIsNeededByAtLeastOneCategory() {
        val primary = ItemCategory.entries.map { it.primarySkill }.toSet()
        assertEquals(Skill.entries.toSet(), primary)
    }

    @Test fun relatedSkillsNeverRepeatThePrimarySkill() {
        ItemCategory.entries.forEach { category ->
            assertFalse(category.primarySkill in category.relatedSkills, "$category")
        }
    }
}
