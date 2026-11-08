// mendfield/shared/src/androidMain/kotlin/mendfield/shared/di/Platform.android.kt
package mendfield.shared.di

import android.content.Context
import mendfield.core.data.CafeCache
import mendfield.core.database.RoomCafeCache
import mendfield.core.database.RoomOutboxStore
import mendfield.core.database.mendfieldDatabase
import mendfield.core.sync.OutboxStore
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

actual fun defaultBaseUrl(): String = "http://10.0.2.2:8086/api/v1/"

// The Context comes from androidContext() in MendfieldApplication.
actual fun platformStorageModule() = module {
    single { mendfieldDatabase(get<Context>()) }.withOptions { onClose { it?.close() } }
    single<CafeCache> { RoomCafeCache(get()) }
    single<OutboxStore> { RoomOutboxStore(get()) }
}
