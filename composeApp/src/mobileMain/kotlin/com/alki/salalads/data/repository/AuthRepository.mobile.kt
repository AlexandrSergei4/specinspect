package com.alki.salalads.data.repository

import co.touchlab.kermit.Logger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Реализация AuthRepository на основе Firebase Auth (gitlive)
 * Для мобильных платформ (Android + iOS)
 */
actual class AuthRepository actual constructor() {
    private val auth: FirebaseAuth = Firebase.auth

    actual val currentUser: AuthUser?
        get() = auth.currentUser?.let { AuthUser(uid = it.uid) }

    actual val currentUserId: String?
        get() = auth.currentUser?.uid

    actual val isLoggedIn: Boolean
        get() = auth.currentUser != null

    // Поток для отслеживания состояния авторизации
    actual val authStateFlow: Flow<AuthUser?> = auth.authStateChanged.map { firebaseUser ->
        firebaseUser?.let { AuthUser(uid = it.uid) }
    }

    actual val isLoggedInFlow: Flow<Boolean> = authStateFlow.map { it != null }

    /**
     * Анонимный вход - создает нового пользователя если не авторизован
     */
    actual suspend fun signInAnonymously(): Result<AuthUser> {
        return try {
            val result = auth.signInAnonymously()
            Result.success(AuthUser(uid = result.user!!.uid))
        } catch (e: Exception) {
            Logger.e("Failed signInAnonymously()", e)
            Result.failure(e)
        }
    }

    /**
     * Выход из аккаунта
     */
    actual suspend fun signOut() {
        auth.signOut()
    }
}
