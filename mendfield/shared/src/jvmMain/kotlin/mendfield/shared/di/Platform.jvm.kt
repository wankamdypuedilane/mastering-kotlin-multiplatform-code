// mendfield/shared/src/jvmMain/kotlin/mendfield/shared/di/Platform.jvm.kt
package mendfield.shared.di

import mendfield.core.data.CafeCache
import mendfield.core.data.TransactionRunner
import mendfield.core.database.RoomCafeCache
import mendfield.core.database.RoomOutboxStore
import mendfield.core.database.RoomTransactionRunner
import mendfield.core.database.mendfieldDatabase
import mendfield.core.sync.OutboxStore
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

actual fun defaultBaseUrl(): String = "http://localhost:8086/api/v1/"

/** ~/.mendfield/mendfield.db: delete it to start from an empty device. */
actual fun platformStorageModule() = module {
    single { mendfieldDatabase() }.withOptions { onClose { it?.close() } }
    single<CafeCache> { RoomCafeCache(get()) }
    single<OutboxStore> { RoomOutboxStore(get()) }
    single<TransactionRunner> { RoomTransactionRunner(get()) } // one file
}
