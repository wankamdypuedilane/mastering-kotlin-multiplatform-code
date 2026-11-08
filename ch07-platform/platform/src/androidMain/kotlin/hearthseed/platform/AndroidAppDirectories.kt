package hearthseed.platform

import android.content.Context

/** The app's private files folder; needs a Context, so it is injected. */
class AndroidAppDirectories(context: Context) : AppDirectories {
    override val data: String = context.filesDir.absolutePath
}
