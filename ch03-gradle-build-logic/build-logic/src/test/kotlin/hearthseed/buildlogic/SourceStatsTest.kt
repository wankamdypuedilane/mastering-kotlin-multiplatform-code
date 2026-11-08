// ch03-gradle-build-logic/build-logic/src/test/kotlin/.../SourceStatsTest.kt
package hearthseed.buildlogic

import kotlin.test.Test
import kotlin.test.assertEquals

class SourceStatsTest {
    @Test
    fun singularForOne() =
        assertEquals("commonMain: 1 file, 1 line", statLine("commonMain", 1, 1))

    @Test
    fun pluralOtherwise() {
        assertEquals("jvmTest: 2 files, 7 lines", statLine("jvmTest", 2, 7))
        assertEquals("webMain: 0 files, 0 lines", statLine("webMain", 0, 0))
    }
}
