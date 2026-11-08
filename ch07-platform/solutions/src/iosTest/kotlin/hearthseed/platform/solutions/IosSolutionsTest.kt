package hearthseed.platform.solutions

import hearthseed.platform.joinPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSTemporaryDirectory

class IosSolutionsTest {
    @Test
    fun processorCountMatchesFoundation() {
        val expected = NSProcessInfo.processInfo.activeProcessorCount.toInt()
        assertEquals(expected, processorCount())
    }

    @Test
    fun fileStoreWritesRealFiles() {
        val folder = joinPath(NSTemporaryDirectory(), "hearthseed-${Uuid.random()}")
        val store = IosTextStore(folder)
        assertNull(store.read("a.csv"))
        store.write("a.csv", "kale;3")
        assertEquals("kale;3", IosTextStore(folder).read("a.csv"))
    }
}
