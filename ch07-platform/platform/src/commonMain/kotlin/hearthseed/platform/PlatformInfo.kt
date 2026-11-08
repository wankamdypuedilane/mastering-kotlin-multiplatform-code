package hearthseed.platform

/** The family of runtime the code was compiled for. */
enum class PlatformKind { ANDROID, JVM, IOS, JS, WASM }

/** What the running platform says about itself. */
data class PlatformInfo(
    val kind: PlatformKind,
    val osName: String,
    val osVersion: String,
) {
    val summary: String get() = "$osName $osVersion (${kind.name.lowercase()})"
}

/** Implemented once per platform, with that platform's own API. */
expect fun platformInfo(): PlatformInfo
