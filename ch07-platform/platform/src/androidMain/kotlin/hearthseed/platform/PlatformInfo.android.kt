package hearthseed.platform

import android.os.Build

actual fun platformInfo(): PlatformInfo = PlatformInfo(
    kind = PlatformKind.ANDROID,
    osName = "Android",
    osVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
)
