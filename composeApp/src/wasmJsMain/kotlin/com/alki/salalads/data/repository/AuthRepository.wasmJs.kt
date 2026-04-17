@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.alki.salalads.data.repository

import com.alki.salalads.firebase.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise

/**
 * Реализация AuthRepository для WASM (браузер)
 * Использует Firebase Auth JS SDK
 */
actual class AuthRepository actual constructor() {
    private val auth: FirebaseAuth = getFirebaseAuth()
    private val _currentUser = MutableStateFlow<AuthUser?>(null)

    actual val currentUser: AuthUser?
        get() = _currentUser.value

    actual val currentUserId: String?
        get() = _currentUser.value?.uid

    actual val isLoggedIn: Boolean
        get() = _currentUser.value != null

    actual val authStateFlow: Flow<AuthUser?> = _currentUser

    actual val isLoggedInFlow: Flow<Boolean> = _currentUser.map { it != null }

    init {
        // Инициализируем с текущим пользователем, если есть
        val user = authCurrentUser(auth)
        if (user != null) {
            _currentUser.value = AuthUser(uid = user.uid)
        }

        // Подписываемся на изменения состояния авторизации
        // Примечание: callback работает через JS interop
        setupAuthStateListener()
    }

    private fun setupAuthStateListener() {
        // Используем js() для установки listener
        authOnAuthStateChanged(auth) { user ->
            _currentUser.value = user?.let { AuthUser(uid = it.uid) }
        }
    }

    /**
     * Анонимный вход через Firebase Auth
     */
    actual suspend fun signInAnonymously(): Result<AuthUser> {
        return try {
            val promise = authSignInAnonymously(auth)
            awaitPromise(promise)

            // После успешного входа получаем пользователя
            val user = authCurrentUser(auth)
            if (user != null) {
                val authUser = AuthUser(uid = user.uid)
                _currentUser.value = authUser
                Result.success(authUser)
            } else {
                Result.failure(Exception("User is null after sign in"))
            }
        } catch (e: Exception) {
            println("Failed signInAnonymously(): ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Выход из аккаунта
     */
    actual suspend fun signOut() {
        try {
            val promise = authSignOut(auth)
            awaitPromise(promise)
            _currentUser.value = null
        } catch (e: Exception) {
            println("Failed signOut(): ${e.message}")
        }
    }
}

/**
 * Ожидание JS Promise
 */
private suspend fun <T : kotlin.js.JsAny?> awaitPromise(promise: Promise<T>): T? {
    return suspendCoroutine { continuation ->
        promise.then(
            onFulfilled = { value ->
                continuation.resume(value)
                null
            },
            onRejected = { error ->
                val message = jsErrorMessage(error)
                continuation.resumeWithException(Exception(message))
                null
            }
        )
    }
}

private fun jsErrorMessage(error: kotlin.js.JsAny): String = js("error.message || String(error)")
