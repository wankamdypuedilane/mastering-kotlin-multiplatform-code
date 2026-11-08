package hearthseed.platform

/**
 * Where the app may keep its own files. Each platform has its own
 * implementation; common code receives one through its constructor.
 */
interface AppDirectories {
    /** The data folder, or null when the platform has no file system. */
    val data: String?
}
