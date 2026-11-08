// ch21-production/secure/src/commonMain/kotlin/hearthseed/secure/Redactor.kt
package hearthseed.secure

/** Removes credentials from text before it reaches a log or a crash report. */
object Redactor {
    const val MASK = "<redacted>"

    /** Characters of a token (RFC 6750 "b64token"): stops at ")" or ",". */
    private const val VALUE = """[A-Za-z0-9._~+/=-]+"""

    // RegexOption, not an inline (?i): JavaScript's RegExp (Kotlin/JS and
    // Kotlin/Wasm) rejects inline flags with "Invalid group".
    private fun rule(pattern: String, replacement: String) =
        Regex(pattern, RegexOption.IGNORE_CASE) to replacement

    private val rules: List<Pair<Regex, String>> = listOf(
        // Authorization: Bearer abc.def
        rule("""(authorization:\s*bearer\s+)$VALUE""", "$1$MASK"),
        // X-Member-Token: demo-token-cleo (any *-token header)
        rule("""([\w-]*token:\s*)$VALUE""", "$1$MASK"),
        // ?token=...&access_token=...&key=...
        rule(
            """([?&](?:token|access_token|api_key|key)=)[^&\s#]+""",
            "$1$MASK",
        ),
        // "token":"..." and "refreshToken":"..." in JSON bodies
        rule("""("\w*token"\s*:\s*)"[^"]*"""", "$1\"$MASK\""),
    )

    fun redact(text: String): String =
        rules.fold(text) { acc, (pattern, replacement) ->
            pattern.replace(acc, replacement)
        }
}
