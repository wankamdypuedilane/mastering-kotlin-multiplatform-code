// mendfield/shared/src/commonMain/kotlin/mendfield/shared/di/Platform.kt
package mendfield.shared.di

import mendfield.core.data.InMemoryCafeCache
import mendfield.core.data.CafeCache
import mendfield.core.data.SequentialWrites
import mendfield.core.data.TransactionRunner
import mendfield.core.sync.InMemoryOutboxStore
import mendfield.core.sync.OutboxStore
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Where the mendfield server runs during development: http://10.0.2.2:8086
 * on the Android emulator (the host's loopback), the page's own origin on
 * the web (through the development proxy), http://localhost:8086 elsewhere.
 */
expect fun defaultBaseUrl(): String

/**
 * Where this platform keeps the device copy and the outbox: Room on
 * Android, iOS and the desktop; memory and localStorage on the web.
 */
expect fun platformStorageModule(): Module

/** Tests and previews: nothing survives the process. */
val inMemoryStorageModule = module {
    single<CafeCache> { InMemoryCafeCache() }
    single<OutboxStore> { InMemoryOutboxStore() }
    single<TransactionRunner> { SequentialWrites }
}
