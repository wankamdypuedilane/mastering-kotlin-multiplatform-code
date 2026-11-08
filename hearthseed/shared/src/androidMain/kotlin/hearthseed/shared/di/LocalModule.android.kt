// hearthseed/shared/src/androidMain/kotlin/hearthseed/shared/di/LocalModule.android.kt
package hearthseed.shared.di

import android.content.Context
import hearthseed.core.database.hearthseedDatabase

// The Context comes from androidContext() in HearthseedApplication.
actual fun platformLocalModule() =
    roomLocalModule { hearthseedDatabase(get<Context>()) }
