package language

import kotlin.test.Test
import kotlin.test.assertEquals

class ContextTest {
    @Test
    fun contextIsPassedImplicitly() {
        assertEquals(
            listOf("sowed 24 cells of Pea", "sowed 24 cells of Kale"),
            contextDemo(),
        )
        val logger = ListLogger()
        assertEquals(10, with(logger) { sowTray("Leek", 10) })
        assertEquals(listOf("sowed 10 cells of Leek"), logger.lines)
    }
}
