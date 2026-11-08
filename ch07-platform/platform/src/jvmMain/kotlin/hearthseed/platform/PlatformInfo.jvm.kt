package hearthseed.platform

actual fun platformInfo(): PlatformInfo = PlatformInfo(
    kind = PlatformKind.JVM,
    osName = System.getProperty("os.name"),
    osVersion = System.getProperty("os.version"),
)
