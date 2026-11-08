// ch07-platform/platform/src/androidHostTest/.../AndroidPlatformTest.kt
package hearthseed.platform

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Runs the Android actual on the development machine's JVM, against the
 * android.jar stubs: Build.VERSION fields are not set there (null and 0).
 */
class AndroidPlatformTest {
    @Test
    fun androidActualReportsAndroid() {
        val info = platformInfo()
        assertEquals(PlatformKind.ANDROID, info.kind)
        assertEquals("Android", info.osName)
        assertEquals("null (API 0)", info.osVersion)   // stub values
    }
}
