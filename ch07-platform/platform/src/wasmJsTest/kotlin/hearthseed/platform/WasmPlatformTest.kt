package hearthseed.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WasmPlatformTest {
    @Test
    fun reportsWasmOnNode() {
        val info = platformInfo()
        assertEquals(PlatformKind.WASM, info.kind)
        assertEquals("Node.js", info.osName)       // tests run with nodejs()
        assertNull(WebAppDirectories.data)
    }
}
