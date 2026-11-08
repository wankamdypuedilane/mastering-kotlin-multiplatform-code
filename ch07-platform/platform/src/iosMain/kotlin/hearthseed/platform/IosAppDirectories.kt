package hearthseed.platform

import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/** Application Support inside the app's sandbox. */
class IosAppDirectories(appName: String) : AppDirectories {
    override val data: String = joinPath(
        NSSearchPathForDirectoriesInDomains(
            NSApplicationSupportDirectory, NSUserDomainMask, true,
        ).first() as String,
        appName,
    )
}
