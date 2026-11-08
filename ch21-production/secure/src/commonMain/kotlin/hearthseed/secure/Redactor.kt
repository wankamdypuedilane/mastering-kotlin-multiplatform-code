// ch21-production/secure/src/commonMain/kotlin/hearthseed/secure/Redactor.kt
package hearthseed.secure

/** Removes credentials from text before it reaches a log or a crash report. */
object Redactor {
    const val MASK = "<redacted>"

    /** Characters of a token (RFC 6750 "b64token"): stops at ")" or ",". */
    private const val VALUE = """[A-Za-z0-9._~+/=-]+"""

    private val rules: List<Pair<Regex, String>> = listOf(
        // Authorization: Bearer abc.def
        Regex("""(?i)(authorization:\s*bearer\s+)$VALUE""") to "$1$MASK",
        // X-Member-Token: demo-token-cleo (any *-token header)
        Regex("""(?i)([\w-]*token:\s*)$VALUE""") to "$1$MASK",
        // ?token=...&access_token=...&key=...
        Regex("""(?i)([?&](?:token|access_token|api_key|key)=)[^&\s#]+""")
            to "$1$MASK",
        // "token":"..." and "refreshToken":"..." in JSON bodies
        Regex("""(?i)("\w*token"\s*:\s*)"[^"]*"""") to "$1\"$MASK\"",
    )

    fun redact(text: String): String =
        rules.fold(text) { acc, (pattern, replacement) ->
            pattern.replace(acc, replacement)
        }
}
