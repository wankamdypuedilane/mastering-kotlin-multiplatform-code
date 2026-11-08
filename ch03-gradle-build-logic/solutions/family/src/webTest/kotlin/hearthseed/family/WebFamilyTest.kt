// ch03-gradle-build-logic/solutions/family/src/webTest/.../WebFamilyTest.kt
package hearthseed.family

import kotlin.test.Test
import kotlin.test.assertEquals

class WebFamilyTest {
    @Test
    fun isWeb() = assertEquals("web", runtimeFamily())
}
