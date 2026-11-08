// ch03-gradle-build-logic/solutions/family/src/commonTest/.../GreetingTest.kt
package hearthseed.family

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetingTest {
    @Test
    fun greetingNamesAFamily() {
        val family = greeting().substringAfterLast(' ')
        assertTrue(family in setOf("jvm", "web", "ios"))
    }
}
