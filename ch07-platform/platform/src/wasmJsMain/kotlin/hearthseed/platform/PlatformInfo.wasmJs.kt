package hearthseed.platform

actual fun platformInfo(): PlatformInfo = PlatformInfo(
    kind = PlatformKind.WASM,
    osName = if (nodeVersion() != null) "Node.js" else "Browser",
    osVersion = nodeVersion() ?: "unknown",
)

// In Kotlin/Wasm, js("...") must be the whole body of a top-level function.
@OptIn(ExperimentalWasmJsInterop::class)
private fun nodeVersion(): String? =
    js("typeof process !== 'undefined' ? process.version : null")
