// ch03-gradle-build-logic/units/src/commonTest/.../GramsTest.kt
package hearthseed.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GramsTest {
    @Test
    fun addsAndCompares() {
        assertEquals(7.grams, 3.grams + 4.grams)
        assertEquals(true, 2.grams < 5.grams)
    }

    @Test
    fun roundsSeedWeightUp() {
        // 400 tomato seeds at 3 g per 1,000 seeds = 1.2 g -> 2 g.
        assertEquals(2.grams, gramsForSeeds(400, thousandSeedWeight = 3.grams))
        assertEquals(0.grams, gramsForSeeds(0, thousandSeedWeight = 3.grams))
    }

    @Test
    fun rejectsNegativeWeight() {
        assertFailsWith<IllegalArgumentException> { Grams(-1) }
    }
}
