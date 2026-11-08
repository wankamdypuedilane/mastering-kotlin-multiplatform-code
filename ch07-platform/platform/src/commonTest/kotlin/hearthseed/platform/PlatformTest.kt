package hearthseed.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

/** Runs on every target: each one checks its own actuals. */
class PlatformTest {
    @Test
    fun platformDescribesItself() {
        val info = platformInfo()
        assertTrue(info.osName.isNotBlank())
        assertTrue(info.summary.endsWith("(${info.kind.name.lowercase()})"))
    }

    @Test
    fun joinPathUsesTheSeparatorOnce() {
        val s = pathSeparator
        assertEquals("a${s}b${s}c", joinPath("a$s", "", "b", "c"))
    }

    @Test
    fun uuidIdsAreVersion4AndUnique() {
        val first = UuidIdGenerator.newId()
        assertEquals(36, first.length)
        assertEquals('4', first[14])              // the version digit
        assertEquals(first, Uuid.parse(first).toString())
        assertNotEquals(first, UuidIdGenerator.newId())
    }
}
