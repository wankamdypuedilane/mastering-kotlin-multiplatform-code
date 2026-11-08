package labels.core

import kotlin.test.Test
import kotlin.test.assertEquals

class VarietyCsvTest {
    @Test
    fun readsGoodLinesAndReportsBadOnes() {
        val csv = """
            id,commonName,latinName,family,days,months,difficulty
            var-010,Larkmoor Pea,Pisum sativum,Fabaceae,60,3 4,easy

            var-099,Broken,Nowhere,Fabaceae,soon,3,EASY
            var-033,Beacon chard,Beta vulgaris,Amaranthaceae,55,4 5 6 7,EASY
        """.trimIndent()

        val result = parseVarieties(csv)

        val ids = result.varieties.map { it.id }
        assertEquals(listOf("var-010", "var-033"), ids)
        assertEquals(setOf(3, 4), result.varieties.first().sowingMonths)
        assertEquals(1, result.errors.size)
        assertEquals(true, result.errors.single().startsWith("Line 4:"))
    }
}
