package hearthseed.labels.android

import android.app.Application
import hearthseed.labels.ui.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/** Starts Koin once per process, before any activity is created. */
class LabelsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@LabelsApplication)
        }
    }
}
