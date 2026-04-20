@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.alki.specinspect.data.storage

import cnames.structs.__CFData
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

class IosUserAccessTokenSecureStorage : UserAccessTokenSecureStorage {

    override fun getToken(): String? = memScoped {
        val query = createBaseQuery().apply {
            CFDictionaryAddValue(this, kSecReturnData, kCFBooleanTrue)
            CFDictionaryAddValue(this, kSecMatchLimit, kSecMatchLimitOne)
        }
        val result = alloc<CPointerVar<__CFData>>()

        when (SecItemCopyMatching(query, result.ptr.reinterpret())) {
            errSecSuccess -> result.value?.toByteArray()?.decodeToString()
            errSecItemNotFound -> null
            else -> null
        }
    }

    override fun saveToken(token: String) {
        val query = createBaseQuery()
        val data = token.encodeToByteArray().toCFData()
        val attributes = CFDictionaryCreateMutable(null, 0, null, null)!!.apply {
            CFDictionaryAddValue(this, kSecValueData, data)
        }

        val status = SecItemUpdate(query, attributes)
        if (status == errSecItemNotFound) {
            val addQuery = createBaseQuery().apply {
                CFDictionaryAddValue(this, kSecAttrAccessible, kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
                CFDictionaryAddValue(this, kSecValueData, data)
            }
            SecItemAdd(addQuery, null)
        }
    }

    override fun clearToken() {
        SecItemDelete(createBaseQuery())
    }
}

private fun ByteArray.toCFData(): CFDataRef? =
    CFDataCreate(null, toUByteArray().refTo(0), size.toLong())

private fun CFDataRef.toByteArray(): ByteArray {
    val result = ByteArray(CFDataGetLength(this).toInt())
    if (result.isEmpty()) return result

    val bytes = CFDataGetBytePtr(this) ?: return result
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, result.size.toULong())
    }
    return result
}

private fun createBaseQuery(): CFDictionaryRef =
    CFDictionaryCreateMutable(null, 0, null, null)!!.apply {
        CFDictionaryAddValue(this, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(this, kSecAttrService, serviceNameRef)
        CFDictionaryAddValue(this, kSecAttrAccount, accountNameRef)
    }

private val serviceNameRef = CFStringCreateWithCString(null, SERVICE_NAME, kCFStringEncodingUTF8)
private val accountNameRef = CFStringCreateWithCString(null, ACCOUNT_NAME, kCFStringEncodingUTF8)

private const val SERVICE_NAME = "com.alki.specinspect.github"
private const val ACCOUNT_NAME = "user_access_token"
