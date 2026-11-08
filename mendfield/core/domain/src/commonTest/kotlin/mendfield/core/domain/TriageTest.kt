// mendfield/core/domain/src/commonTest/kotlin/mendfield/core/domain/TriageTest.kt
package mendfield.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import mendfield.core.domain.Fixtures.callum
import mendfield.core.domain.Fixtures.grid
import mendfield.core.domain.Fixtures.ines
import mendfield.core.domain.Fixtures.maud
import mendfield.core.domain.Fixtures.tomasz
import mendfield.core.model.Fixer
import mendfield.core.model.ItemCategory
import mendfield.core.model.Skill

class TriageTest {
    private val everyone = listOf(maud, tomasz, ines, callum)

    private fun names(category: ItemCategory, slots: List<mendfield.core.model.RepairSlot>) =
        suggestFixers(category, everyone, slots).map { it.fixer.displayName to it.match }

    @Test fun primarySkillComesBeforeRelatedSkills() {
        // BICYCLE: primary BICYCLES (Tomasz), related MECHANICAL (Callum).
        assertEquals(
            listOf("Tomasz" to SkillMatch.PRIMARY, "Callum" to SkillMatch.RELATED),
            names(ItemCategory.BICYCLE, emptyList()),
        )
    }

    @Test fun theLighterLoadWinsBetweenEqualMatches() {
        val busyMaud = grid(maud).take(2).mapIndexed { i, s -> s.copy(itemId = "i$i") }
        val ranked = suggestFixers(ItemCategory.LAMP, everyone, busyMaud + grid(callum))
        assertEquals(listOf("Callum", "Maud"), ranked.map { it.fixer.displayName })
        assertEquals(listOf(0, 2), ranked.map { it.load })
    }

    @Test fun moreRelatedSkillsBreakATie() {
        // LAMP: related ELECTRONICS. Maud has it, Callum does not.
        assertEquals(listOf("Maud", "Callum"),
            names(ItemCategory.LAMP, emptyList()).map { it.first })
    }

    @Test fun nameAndIdMakeTheOrderTotal() {
        val twinA = Fixer("fx-b", "Sam", setOf(Skill.SEWING))
        val twinB = Fixer("fx-a", "Sam", setOf(Skill.SEWING))
        val ranked = suggestFixers(ItemCategory.CLOTHING, listOf(twinA, twinB, ines), emptyList())
        assertEquals(listOf("fx-3", "fx-a", "fx-b"), ranked.map { it.fixer.id })
    }

    @Test fun nobodyQualifiesForAnUnknownSkill() {
        assertEquals(emptyList(), suggestFixers(ItemCategory.FURNITURE, everyone, emptyList()))
    }

    @Test fun openSlotsDoNotCountAsLoad() {
        val ranked = suggestFixers(ItemCategory.CLOTHING, everyone, grid(ines))
        assertEquals(0, ranked.single().load)
    }
}
