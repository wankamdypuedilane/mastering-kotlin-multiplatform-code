package hearthseed.platform

import kotlin.test.Test
import kotlin.test.assertEquals

class JvmPlatformTest {
    @Test
    fun reportsTheJvmAndTheHostSystem() {
        val info = platformInfo()
        assertEquals(PlatformKind.JVM, info.kind)
        assertEquals(System.getProperty("os.name"), info.osName)
    }

    @Test
    fun dataDirectoryFollowsEachSystemConvention() {
        val noEnv: (String) -> String? = { null }
        assertEquals(
            "/Users/ada/Library/Application Support/Hearthseed",
            desktopDataDirectory("Hearthseed", "Mac OS X", "/Users/ada", noEnv),
        )
        assertEquals(
            "C:\\Users\\ada\\AppData\\Roaming\\Hearthseed",
            desktopDataDirectory(
                "Hearthseed", "Windows 11", "C:\\Users\\ada", noEnv,
            ),
        )
        assertEquals(
            "/home/ada/.local/share/Hearthseed",
            desktopDataDirectory("Hearthseed", "Linux", "/home/ada", noEnv),
        )
        assertEquals(
            "/xdg/Hearthseed",
            desktopDataDirectory("Hearthseed", "Linux", "/home/ada") { "/xdg" },
        )
    }
}
