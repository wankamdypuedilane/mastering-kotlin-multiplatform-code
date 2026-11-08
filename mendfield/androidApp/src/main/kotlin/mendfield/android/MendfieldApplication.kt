// mendfield/androidApp/src/main/kotlin/mendfield/android/MendfieldApplication.kt
package mendfield.android

import android.app.Application
import mendfield.shared.di.AppConfig
import mendfield.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

/** Starts Koin, Room and the sync loop once per process, before any activity. */
class MendfieldApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(AppConfig(baseUrl = BuildConfig.BASE_URL)) {
            androidContext(this@MendfieldApplication) // Room's Context
        }
    }
}
