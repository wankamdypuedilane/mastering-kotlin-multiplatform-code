package language

import kotlin.test.Test
import kotlin.test.assertEquals

class LambdasTest {
    @Test
    fun functionTypes() {
        assertEquals(8, double(4))
        assertEquals(7, add(3, 4))
        assertEquals(
            listOf(81, listOf("PEA", "KALE"), listOf(2, 4, 6), 7),
            lambdaDemo(),
        )
    }

    @Test
    fun scopeFunctions() {
        val log = mutableListOf<String>()
        assertEquals(
            listOf("Variety: Leek", 10, "A: 10/24", 10, "Kale"),
            scopeDemo("Leek", log),
        )
        assertEquals(listOf("created Kale"), log)
        assertEquals("no name", scopeDemo(null, log).first())
    }
}
