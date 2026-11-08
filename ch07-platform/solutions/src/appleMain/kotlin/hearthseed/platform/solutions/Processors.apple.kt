package hearthseed.platform.solutions

import platform.Foundation.NSProcessInfo

// appleMain: the same actual for iOS, and later macOS, tvOS or watchOS.
actual fun processorCount(): Int =
    NSProcessInfo.processInfo.activeProcessorCount.toInt()
