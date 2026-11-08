// hearthseed/webApp/support/src/jsMain/kotlin/hearthseed/web/support/Exports.kt
package hearthseed.web.support

import kotlin.js.JsExport

/**
 * For the library's plain JavaScript pages: the names of the favorites that
 * the app saved, read with the same codec. Kotlin/JS only.
 */
@OptIn(ExperimentalJsExport::class) // @JsExport is still Experimental
@JsExport
fun favoriteNames(storedJson: String?): Array<String> =
    FavoritesCodec.decode(storedJson).map { it.name }.toTypedArray()
