package hearthseed.labels.ui

import org.koin.dsl.module

actual val platformModule = module {
    single { PrinterHost("Web") }
}
