// ch21-production/secure/src/webMain/kotlin/hearthseed/secure/SecureStorage.web.kt
package hearthseed.secure

/*
 * Browser: localStorage and IndexedDB are readable by every script on the
 * origin, so one XSS bug leaks a token kept there. Prefer an HttpOnly session
 * cookie set by the server; keep anything else in memory.
 */
actual val secureStorageKind: SecureStorageKind = SecureStorageKind.MEMORY_ONLY
