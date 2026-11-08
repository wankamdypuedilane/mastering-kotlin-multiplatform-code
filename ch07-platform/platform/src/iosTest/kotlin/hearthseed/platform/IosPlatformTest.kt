package hearthseed.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IosPlatformTest {
    @Test
    fun reportsIos() {
        assertEquals(PlatformKind.IOS, platformInfo().kind)
    }

    @Test
    fun dataDirectoryIsApplicationSupport() {
        val data = IosAppDirectories("Hearthseed").data
        assertTrue(data.endsWith("/Library/Application Support/Hearthseed"), data)
    }
}
