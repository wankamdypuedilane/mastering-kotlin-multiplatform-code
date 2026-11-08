// ch03-gradle-build-logic/solutions/family/src/jvmTest/.../JvmFamilyTest.kt
package hearthseed.family

import kotlin.test.Test
import kotlin.test.assertEquals

class JvmFamilyTest {
    @Test
    fun isJvm() = assertEquals("jvm", runtimeFamily())
}
