package hearthseed.platform

actual fun platformInfo(): PlatformInfo = PlatformInfo(
    kind = PlatformKind.JS,
    osName = if (nodeVersion() != null) "Node.js" else "Browser",
    osVersion = nodeVersion() ?: "unknown",
)

private fun nodeVersion(): String? =
    js("typeof process !== 'undefined' ? process.version : null")
