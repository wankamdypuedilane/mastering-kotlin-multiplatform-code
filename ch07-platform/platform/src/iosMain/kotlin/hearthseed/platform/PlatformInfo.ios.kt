package hearthseed.platform

import platform.UIKit.UIDevice

actual fun platformInfo(): PlatformInfo {
    val device = UIDevice.currentDevice
    return PlatformInfo(
        kind = PlatformKind.IOS,
        osName = device.systemName(),
        osVersion = device.systemVersion,
    )
}
