// ch22-release/solutions/src/commonTest/kotlin/release/solutions/SolutionsTest.kt
package release.solutions

import release.MAX_VERSION_CODE
import release.ReleaseVersion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SolutionsTest {
    private val v = ReleaseVersion(1, 4, 2)

    @Test
    fun bumpResetsLowerParts() {
        assertEquals(ReleaseVersion(2, 0, 0), v.bump(Bump.MAJOR))
        assertEquals(ReleaseVersion(1, 5, 0), v.bump(Bump.MINOR))
        assertEquals(ReleaseVersion(1, 4, 3), v.bump(Bump.PATCH))
    }

    @Test
    fun versionCodeEncodesEveryPart() {
        assertEquals(1_040_207, androidVersionCode(v, 7))
        assertEquals(2_099_999_999, androidVersionCode(ReleaseVersion(2099, 99, 99), 99))
        assertTrue(androidVersionCode(ReleaseVersion(2099, 99, 99), 99) <= MAX_VERSION_CODE)
    }

    @Test
    fun newerVersionsGetGreaterCodes() {
        val ordered = listOf(
            androidVersionCode(v, 7), androidVersionCode(v, 8),
            androidVersionCode(v.bump(Bump.PATCH), 0),
            androidVersionCode(v.bump(Bump.MINOR), 0),
            androidVersionCode(v.bump(Bump.MAJOR), 0),
        )
        assertEquals(ordered.sorted(), ordered)
    }

    @Test
    fun rejectsWhatDoesNotFit() {
        assertFailsWith<IllegalArgumentException> { androidVersionCode(ReleaseVersion(1, 100, 0), 0) }
        assertFailsWith<IllegalArgumentException> { androidVersionCode(v, 100) }
        assertFailsWith<IllegalArgumentException> { androidVersionCode(ReleaseVersion(2100, 0, 0), 0) }
        assertFailsWith<IllegalArgumentException> { androidVersionCode(ReleaseVersion(0, 0, 0), 0) }
    }
}
