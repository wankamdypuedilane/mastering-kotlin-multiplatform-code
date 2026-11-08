package language

import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionsTest {
    @Test
    fun basics() {
        assertEquals(listOf(2, 3, listOf("Pea", "Leek")), collectionBasics())
    }

    @Test
    fun transformationResults() {
        val expected = listOf(
            listOf("Gold Drop", "Scarlet Runner", "Winter Kale"),
            mapOf("Solanaceae" to 2, "Fabaceae" to 1, "Brassicaceae" to 1),
            65,
            2 to 2,
            255,
            "Larkmoor Red",
            "Winter Kale",
        )
        assertEquals(expected, transformations())
    }

    @Test
    fun sequencesAreLazy() {
        assertEquals(
            listOf(
                "map 1", "map 2", "map 3", "map 4",
                "filter 10", "filter 20", "filter 30", "filter 40",
            ),
            evaluationLog(lazy = false),
        )
        assertEquals(
            listOf("map 1", "filter 10", "map 2", "filter 20"),
            evaluationLog(lazy = true),
        )
        assertEquals(listOf(1, 2, 4, 8, 16, 32, 64), doublingSeeds())
    }
}
