// ch21-production/secure/src/iosMain/kotlin/hearthseed/secure/KeychainSecureStore.kt
package hearthseed.secure

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecDuplicateItem
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

actual val secureStorageKind: SecureStorageKind =
    SecureStorageKind.APPLE_KEYCHAIN

/** A Keychain call failed with this OSStatus (see SecBase.h). */
class KeychainException(val operation: String, val status: Int) :
    Exception("$operation failed with OSStatus $status")

/** Generic-password items in the Keychain, one per key, under [service]. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class KeychainSecureStore(private val service: String) : SecureStore {

    override suspend fun read(key: String): String? = memScoped {
        val query = itemQuery(key) // class, service and account
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        CFRelease(query)
        when (status) {
            errSecSuccess -> {
                val data = CFBridgingRelease(result.value) as NSData
                NSString.create(data = data, encoding = NSUTF8StringEncoding)
                    ?.toString()
            }
            errSecItemNotFound -> null
            else -> throw KeychainException("SecItemCopyMatching", status)
        }
    }

    override suspend fun write(key: String, value: String) {
        val data = NSString.create(string = value)
            .dataUsingEncoding(NSUTF8StringEncoding)
            ?: throw IllegalArgumentException("Value is not valid UTF-8")
        val bridgedData = CFBridgingRetain(data)
        val item = itemQuery(key)
        CFDictionaryAddValue(item, kSecValueData, bridgedData)
        CFDictionaryAddValue(
            item, kSecAttrAccessible,
            kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        )
        var operation = "SecItemAdd"
        var status = SecItemAdd(item, null)
        if (status == errSecDuplicateItem) {
            operation = "SecItemUpdate"
            val query = itemQuery(key)
            val changes = dictionary()
            CFDictionaryAddValue(changes, kSecValueData, bridgedData)
            status = SecItemUpdate(query, changes)
            CFRelease(query)
            CFRelease(changes)
        }
        CFRelease(item)
        CFRelease(bridgedData)
        if (status != errSecSuccess) throw KeychainException(operation, status)
    }

    override suspend fun delete(key: String) {
        val query = itemQuery(key)
        val status = SecItemDelete(query)
        CFRelease(query)
        if (status != errSecSuccess && status != errSecItemNotFound) {
            throw KeychainException("SecItemDelete", status)
        }
    }

    /** class + service + account: identifies exactly one item. */
    private fun itemQuery(key: String): CFMutableDictionaryRef? {
        val query = dictionary()
        val bridgedService =
            CFBridgingRetain(NSString.create(string = service))
        val bridgedAccount = CFBridgingRetain(NSString.create(string = key))
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, bridgedService)
        CFDictionaryAddValue(query, kSecAttrAccount, bridgedAccount)
        CFRelease(bridgedService) // the dictionary keeps its own reference
        CFRelease(bridgedAccount)
        return query
    }

    private fun dictionary(): CFMutableDictionaryRef? = CFDictionaryCreateMutable(
        null, 0.convert(), kCFTypeDictionaryKeyCallBacks.ptr,
        kCFTypeDictionaryValueCallBacks.ptr,
    )
}
