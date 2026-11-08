// hearthseed/webApp/support/src/commonTest/kotlin/hearthseed/web/support/ApiBaseUrlTest.kt
package hearthseed.web.support

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiBaseUrlTest {
    private val origin = "http://localhost:8080"

    @Test
    fun defaultIsTheProxiedPathOnTheSameOrigin() {
        assertEquals("http://localhost:8080/api/v1/", apiBaseUrl(null, origin))
        assertEquals("http://localhost:8080/api/v1/", apiBaseUrl("  ", origin))
    }

    @Test
    fun aConfiguredPathOrUrlWinsAndEndsWithSlash() {
        assertEquals("https://seeds.example.org/api/v2/",
            apiBaseUrl("/api/v2", "https://seeds.example.org/"))
        assertEquals("https://api.example.org/v1/",
            apiBaseUrl("https://api.example.org/v1", origin))
    }
}
