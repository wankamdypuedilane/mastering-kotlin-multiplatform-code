package hearthseed.platform.solutions

// Browsers and recent Node.js versions expose navigator.hardwareConcurrency.
@OptIn(ExperimentalWasmJsInterop::class)
actual fun processorCount(): Int = js(
    "(typeof navigator !== 'undefined' && navigator.hardwareConcurrency) || 1"
)
