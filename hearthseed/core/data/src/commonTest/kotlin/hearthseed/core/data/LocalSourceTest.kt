// hearthseed/core/data/src/commonTest/kotlin/hearthseed/core/data/LocalSourceTest.kt
package hearthseed.core.data

import hearthseed.core.data.local.InMemoryVarietyLocalSource
import hearthseed.core.model.Difficulty
import hearthseed.core.model.Variety
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class LocalSourceTest {
    private fun variety(id: String, name: String, family: String) =
        Variety(id, name, "Latin $name", family, 60, setOf(4), Difficulty.EASY, "")

    @Test fun searchMatchesNamesIgnoringCaseAndFiltersByFamily() = runTest {
        val local = InMemoryVarietyLocalSource()
        local.saveVarieties(listOf(
            variety("var-2", "Quarry Gold tomato", "Solanaceae"),
            variety("var-1", "Larkmoor Ember tomato", "Solanaceae"),
            variety("var-3", "Larkmoor Pea", "Fabaceae"),
        ))
        assertEquals(listOf("var-1", "var-2"), local.search("TOMATO", null).map { it.id })
        assertEquals(listOf("var-3"), local.search("larkmoor", "fabaceae").map { it.id })
    }
}
