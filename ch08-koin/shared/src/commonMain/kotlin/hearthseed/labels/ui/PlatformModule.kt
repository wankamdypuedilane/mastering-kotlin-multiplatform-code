package hearthseed.labels.ui

import org.koin.core.module.Module

/** Describes the device the labels are printed from. */
class PrinterHost(val name: String)

/** Definitions that only each platform can provide. */
expect val platformModule: Module
