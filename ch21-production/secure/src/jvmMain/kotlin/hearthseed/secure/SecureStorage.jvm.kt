// ch21-production/secure/src/jvmMain/kotlin/hearthseed/secure/SecureStorage.jvm.kt
package hearthseed.secure

/*
 * Desktop: macOS, Windows and Linux each have a credential store, but the
 * JDK has no API for them. Until you add native interop or a library you
 * trust, the desktop app keeps the session in memory (InMemorySecureStore)
 * and asks the member to sign in again after a restart.
 */
actual val secureStorageKind: SecureStorageKind = SecureStorageKind.MEMORY_ONLY
