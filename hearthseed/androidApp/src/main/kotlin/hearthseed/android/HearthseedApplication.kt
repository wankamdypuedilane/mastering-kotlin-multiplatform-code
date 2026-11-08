// hearthseed/androidApp/src/main/kotlin/hearthseed/android/HearthseedApplication.kt
package hearthseed.android

import android.app.Application
import hearthseed.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

/** Starts Koin once per process, before any activity (Chapter 8). */
class HearthseedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@HearthseedApplication) }
    }
}
