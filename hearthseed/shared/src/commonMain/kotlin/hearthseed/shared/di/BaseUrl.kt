// hearthseed/shared/src/commonMain/kotlin/hearthseed/shared/di/BaseUrl.kt
package hearthseed.shared.di

/**
 * Where hearthseed-server runs during development: http://10.0.2.2:8085 on
 * the Android emulator (the host's loopback), http://localhost:8085 elsewhere.
 */
expect fun defaultBaseUrl(): String
