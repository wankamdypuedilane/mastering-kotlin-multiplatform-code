// hearthseed/webApp/support/src/commonMain/kotlin/hearthseed/web/support/ApiBaseUrl.kt
package hearthseed.web.support

/**
 * The API base for a page served from [origin] ("http://localhost:8080").
 * [configured] comes from the page (index.html) and wins; it may be absolute
 * ("https://api.example.org/v1/") or a path on the same origin ("/api/v1/").
 * Without it the app calls "/api/v1/" on its own origin, which the
 * development server forwards to hearthseed-server.
 */
fun apiBaseUrl(configured: String?, origin: String): String {
    val value = configured?.trim().orEmpty().ifEmpty { DEFAULT_API_PATH }
    val absolute =
        if (value.startsWith("/")) origin.trimEnd('/') + value else value
    return if (absolute.endsWith("/")) absolute else "$absolute/"
}

const val DEFAULT_API_PATH = "/api/v1/"
