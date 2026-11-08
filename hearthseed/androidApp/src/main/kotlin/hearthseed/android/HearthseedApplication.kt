// hearthseed/androidApp/src/main/kotlin/hearthseed/android/HearthseedApplication.kt
package hearthseed.android

import android.app.Application
import hearthseed.shared.di.AppConfig
import hearthseed.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

/** Starts Koin once per process, before any activity (Chapter 8). */
class HearthseedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // BuildConfig is generated per build type of this module (Chapter 17).
        initKoin(AppConfig(baseUrl = BuildConfig.BASE_URL)) {
            androidContext(this@HearthseedApplication) // Room's Context
        }
    }
}
