// hearthseed/webApp/src/wasmJsMain/kotlin/hearthseed/web/PageConfig.kt
@file:OptIn(ExperimentalWasmJsInterop::class) // JsAny and js() need it

package hearthseed.web

/**
 * The object that index.html may define before the app starts:
 * window.hearthseedConfig = { apiBaseUrl: "/api/v1/" }. An external
 * interface describes a JavaScript object; it has no Kotlin implementation.
 */
external interface PageConfig : JsAny {
    val apiBaseUrl: String? // a missing property arrives as null
}

/** The page's configuration, or null when index.html sets none. */
fun pageConfig(): PageConfig? = js("globalThis.hearthseedConfig || null")
