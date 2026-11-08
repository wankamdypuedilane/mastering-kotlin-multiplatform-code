package hearthseed.platform

/** A browser page has no file system to write to. */
object WebAppDirectories : AppDirectories {
    override val data: String? = null
}
