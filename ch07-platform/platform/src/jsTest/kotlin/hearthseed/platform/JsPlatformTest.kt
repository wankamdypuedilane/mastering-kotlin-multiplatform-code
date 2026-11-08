package hearthseed.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JsPlatformTest {
    @Test
    fun reportsJsOnNode() {
        val info = platformInfo()
        assertEquals(PlatformKind.JS, info.kind)
        assertEquals("Node.js", info.osName)       // tests run with nodejs()
        assertNull(WebAppDirectories.data)
    }
}
