package hearthseed.labels.ui

import org.koin.dsl.module
import platform.UIKit.UIDevice

actual val platformModule = module {
    single { PrinterHost("iOS (${UIDevice.currentDevice.systemVersion})") }
}
