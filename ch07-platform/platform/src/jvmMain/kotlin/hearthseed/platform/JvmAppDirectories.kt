package hearthseed.platform

/** Desktop data folder, following each operating system's convention. */
class JvmAppDirectories(appName: String) : AppDirectories {
    override val data: String = desktopDataDirectory(
        appName = appName,
        osName = System.getProperty("os.name"),
        home = System.getProperty("user.home"),
        env = System::getenv,
    )
}

/** Pure, so that a test can check all three systems on any machine. */
internal fun desktopDataDirectory(
    appName: String,
    osName: String,
    home: String,
    env: (String) -> String?,
): String = when {
    osName.startsWith("Mac") -> "$home/Library/Application Support/$appName"
    osName.startsWith("Windows") ->
        (env("APPDATA") ?: "$home\\AppData\\Roaming") + "\\" + appName
    else -> (env("XDG_DATA_HOME") ?: "$home/.local/share") + "/" + appName
}
