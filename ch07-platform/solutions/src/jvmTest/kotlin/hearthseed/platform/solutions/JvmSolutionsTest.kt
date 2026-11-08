package hearthseed.platform.solutions

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JvmSolutionsTest {
    @Test
    fun processorCountMatchesTheRuntime() {
        assertEquals(Runtime.getRuntime().availableProcessors(), processorCount())
    }

    @Test
    fun fileStoreWritesRealFiles() {
        val folder = Files.createTempDirectory("hearthseed").resolve("exports")
        val store = FileTextStore(folder.toString())
        assertNull(store.read("a.csv"))
        store.write("a.csv", "kale;3")
        assertEquals("kale;3", FileTextStore(folder.toString()).read("a.csv"))
    }
}
