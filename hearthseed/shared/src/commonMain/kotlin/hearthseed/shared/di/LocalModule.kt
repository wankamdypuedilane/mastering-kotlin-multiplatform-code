// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/di/LocalModule.kt
package hearthseed.shared.di

import hearthseed.core.data.local.VarietyLocalSource
import hearthseed.core.database.HearthseedDatabase
import hearthseed.core.database.RoomVarietyLocalSource
import org.koin.core.module.Module
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.withOptions
import org.koin.core.scope.Scope
import org.koin.dsl.module

/** Where this platform keeps the device copy of the catalog (Chapter 14). */
expect fun platformLocalModule(): Module

/** Room behind the seam of Chapter 12: one database for the whole app. */
internal fun roomLocalModule(open: Scope.() -> HearthseedDatabase) = module {
    single { open() }.withOptions { onClose { it?.close() } } // on Koin stop
    single<VarietyLocalSource> { RoomVarietyLocalSource(get()) }
}
