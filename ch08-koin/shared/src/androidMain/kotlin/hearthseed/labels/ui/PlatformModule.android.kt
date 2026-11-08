package hearthseed.labels.ui

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// androidContext() is the Context given to startKoin in the Application class.
actual val platformModule = module {
    single { PrinterHost("Android (${androidContext().packageName})") }
}
