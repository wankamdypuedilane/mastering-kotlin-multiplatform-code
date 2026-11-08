// ch21-production/build-logic/src/test/kotlin/hearthseed/buildlogic/AppConfigRendererTest.kt
package hearthseed.buildlogic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppConfigRendererTest {
    @Test
    fun keysBecomeTypedConstants() {
        val source = AppConfigRenderer.render(
            "demo", "AppConfig",
            mapOf("apiBaseUrl" to "https://x.example", "timeoutSeconds" to "15", "beta" to "true"),
        )
        assertTrue("const val API_BASE_URL: String = \"https://x.example\"" in source)
        assertTrue("const val TIMEOUT_SECONDS: Int = 15" in source)
        assertTrue("const val BETA: Boolean = true" in source)
    }

    @Test
    fun quotesAndDollarsAreEscaped() {
        val source = AppConfigRenderer.render("demo", "C", mapOf("motto" to "say \"hi\" \$5"))
        assertTrue("\"say \\\"hi\\\" \\\$5\"" in source)
    }

    @Test
    fun secretLookingNamesFailTheBuild() {
        val error = assertFailsWith<IllegalArgumentException> {
            AppConfigRenderer.render("demo", "C", mapOf("mapsApiKey" to "YOUR_API_KEY"))
        }
        assertTrue("looks like a secret" in error.message.orEmpty())
    }

    @Test
    fun constantNamesAreScreamingSnakeCase() {
        assertEquals("REQUEST_TIMEOUT_SECONDS", AppConfigRenderer.constantName("requestTimeoutSeconds"))
    }
}
