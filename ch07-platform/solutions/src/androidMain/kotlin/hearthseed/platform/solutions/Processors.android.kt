package hearthseed.platform.solutions

actual fun processorCount(): Int = Runtime.getRuntime().availableProcessors()
