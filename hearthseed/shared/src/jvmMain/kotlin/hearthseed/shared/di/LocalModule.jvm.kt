// hearthseed/shared/src/jvmMain/kotlin/hearthseed/shared/di/LocalModule.jvm.kt
package hearthseed.shared.di

import hearthseed.core.database.hearthseedDatabase

actual fun platformLocalModule() = roomLocalModule { hearthseedDatabase() }
