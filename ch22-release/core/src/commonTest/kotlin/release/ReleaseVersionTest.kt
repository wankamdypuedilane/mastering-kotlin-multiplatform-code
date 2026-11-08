// ch22-release/core/src/commonTest/kotlin/release/ReleaseVersionTest.kt
package release

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReleaseVersionTest {
    @Test
    fun parsesThreePartVersions() {
        assertEquals(ReleaseVersion(1, 4, 2), ReleaseVersion.parse(" 1.4.2 "))
        assertEquals("1.4.2", ReleaseVersion.parse("1.4.2").toString())
    }

    @Test
    fun rejectsOtherShapes() {
        for (bad in listOf("1.4", "1.4.2.0", "v1.4.2", "1.4.x", "", "1..2")) {
            assertFailsWith<IllegalArgumentException>(bad) { ReleaseVersion.parse(bad) }
        }
    }

    @Test
    fun checksMsiLimits() {
        assertTrue(ReleaseVersion(255, 255, 65535).fitsMsi)
        assertFalse(ReleaseVersion(256, 0, 0).fitsMsi)
        assertFalse(ReleaseVersion(1, 0, 65536).fitsMsi)
    }

    @Test
    fun buildNumbersStayInPlayRange() {
        assertEquals(7, checkBuildNumber(7))
        assertEquals(MAX_VERSION_CODE, checkBuildNumber(MAX_VERSION_CODE))
        assertFailsWith<IllegalArgumentException> { checkBuildNumber(0) }
        assertFailsWith<IllegalArgumentException> { checkBuildNumber(MAX_VERSION_CODE + 1) }
    }
}
