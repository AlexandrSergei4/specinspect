package com.alki.specinspect.data.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.alki.specinspect.localization.AppTextKey
import com.alki.specinspect.localization.localizedError
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidUserAccessTokenSecureStorage(
    context: Context,
) : UserAccessTokenSecureStorage {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getToken(): String? {
        val encryptedToken = preferences.getString(TOKEN_KEY, null) ?: return null
        return runCatching { decrypt(encryptedToken) }
            .onFailure { preferences.edit().remove(TOKEN_KEY).apply() }
            .getOrNull()
    }

    override fun saveToken(token: String) {
        preferences.edit().putString(TOKEN_KEY, encrypt(token)).apply()
    }

    override fun clearToken() {
        preferences.edit().remove(TOKEN_KEY).apply()
    }

    private fun encrypt(token: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val encrypted = cipher.doFinal(token.encodeToByteArray())
        val combined = cipher.iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encryptedToken: String): String {
        val combined = Base64.decode(encryptedToken, Base64.NO_WRAP)
        if (combined.size <= IV_SIZE) localizedError(AppTextKey.ErrorInvalidTokenData)

        val iv = combined.copyOfRange(0, IV_SIZE)
        val encrypted = combined.copyOfRange(IV_SIZE, combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(TAG_SIZE_BITS, iv))
        return cipher.doFinal(encrypted).decodeToString()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }
}

private const val PREFERENCES_NAME = "specinspect.secure.storage"
private const val TOKEN_KEY = "github_user_access_token"
private const val KEY_ALIAS = "specinspect.user.access.token"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val IV_SIZE = 12
private const val TAG_SIZE_BITS = 128
