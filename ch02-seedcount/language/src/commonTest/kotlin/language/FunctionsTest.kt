package language

import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionsTest {
    @Test
    fun defaultAndNamedArguments() {
        assertEquals(13, seedsToSow(10))
        assertEquals(15, seedsToSow(10, spares = 2))
        assertEquals(20, seedsToSow(plants = 10, ratePercent = 50))
    }

    @Test
    fun extensions() {
        assertEquals(4, 50.ceilDiv(15))
        assertEquals("LR", "Larkmoor  red".initials)
    }

    @Test
    fun infixVarargAndLocalFunctions() {
        assertEquals(Bed(4, 12), 4 rowsOf 12)
        assertEquals(100, totalSeeds(25, 25, 50))
        val stock = intArrayOf(10, 20)
        assertEquals(35, totalSeeds(*stock, 5))
        assertEquals("1 row of 12 plants", bedReport(1 rowsOf 12))
    }
}
