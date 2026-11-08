// ch22-release/core/src/commonMain/kotlin/release/StaticHosting.kt
package release

/** The Content-Type a static host must send for a file of the web build. */
fun contentTypeFor(path: String): String {
    val extension = path.substringAfterLast('/').substringAfterLast('.', "")
    return when (extension.lowercase()) {
        "wasm" -> "application/wasm" // streaming compilation needs exactly this
        "js", "mjs" -> "text/javascript"
        "html" -> "text/html; charset=utf-8"
        "css" -> "text/css"
        "json", "map" -> "application/json"
        "png" -> "image/png"
        "svg" -> "image/svg+xml"
        "ttf" -> "font/ttf"
        else -> "application/octet-stream"
    }
}

// 16 or more hex digits between separators, as in "app.3f9a0c1b2d4e5f60.wasm".
private val contentHash = Regex("""(^|[.\-_])[0-9a-f]{16,}([.\-_]|$)""")

/**
 * Files whose name changes with their content may be cached for a year.
 * Everything else, index.html first, must be revalidated on each visit.
 */
fun cacheControlFor(path: String): String {
    val name = path.substringAfterLast('/')
    val hashed = name != "index.html" && contentHash.containsMatchIn(name)
    return if (hashed) "public, max-age=31536000, immutable" else "no-cache"
}

/** Text and Wasm compress well; images and fonts usually do not. */
fun isCompressible(path: String): Boolean {
    val type = contentTypeFor(path)
    return type.startsWith("text/") || type == "application/wasm" ||
        type == "application/json" || type == "image/svg+xml"
}
