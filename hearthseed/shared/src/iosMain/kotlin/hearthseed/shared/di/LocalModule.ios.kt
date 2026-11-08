// hearthseed/shared/src/iosMain/kotlin/hearthseed/shared/di/LocalModule.ios.kt
package hearthseed.shared.di

import hearthseed.core.database.hearthseedDatabase

actual fun platformLocalModule() = roomLocalModule { hearthseedDatabase() }
