// mendfield/shared/src/wasmJsMain/kotlin/mendfield/shared/di/Platform.wasmJs.kt
package mendfield.shared.di

import kotlinx.browser.window
import mendfield.core.data.CafeCache
import mendfield.core.data.InMemoryCafeCache
import mendfield.core.data.KeyValueOutboxStore
import mendfield.core.data.localStorageOrMemory
import mendfield.core.sync.OutboxStore
import org.koin.dsl.module

/** The page's own origin: the development server forwards /api to 8086. */
actual fun defaultBaseUrl(): String = "${window.location.origin}/api/v1/"

/**
 * No Room on the web in Mendfield: Room 3's web driver needs a worker script
 * the app would have to serve. The board lives in memory (a reload fetches
 * it again); the outbox lives in localStorage, so queued work survives.
 */
actual fun platformStorageModule() = module {
    single<CafeCache> { InMemoryCafeCache() }
    single<OutboxStore> { KeyValueOutboxStore(localStorageOrMemory()) }
}
