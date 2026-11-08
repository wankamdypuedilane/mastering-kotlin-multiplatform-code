// mendfield/shared/src/iosMain/kotlin/mendfield/shared/di/Platform.ios.kt
package mendfield.shared.di

import mendfield.core.data.CafeCache
import mendfield.core.database.RoomCafeCache
import mendfield.core.database.RoomOutboxStore
import mendfield.core.database.mendfieldDatabase
import mendfield.core.sync.OutboxStore
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

/** The simulator shares the Mac's loopback; a device needs the Mac's IP. */
actual fun defaultBaseUrl(): String = "http://localhost:8086/api/v1/"

actual fun platformStorageModule() = module {
    single { mendfieldDatabase() }.withOptions { onClose { it?.close() } }
    single<CafeCache> { RoomCafeCache(get()) }
    single<OutboxStore> { RoomOutboxStore(get()) }
}
