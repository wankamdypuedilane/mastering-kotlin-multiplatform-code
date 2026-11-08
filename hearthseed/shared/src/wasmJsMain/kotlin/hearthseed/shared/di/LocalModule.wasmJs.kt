// hearthseed/shared/src/wasmJsMain/kotlin/hearthseed/shared/di/LocalModule.wasmJs.kt
package hearthseed.shared.di

// Room 3 compiles for the web, but its WebWorkerSQLiteDriver needs a worker
// script that the app must serve (Chapter 18). Until then: memory only.
actual fun platformLocalModule() = inMemoryLocalModule
