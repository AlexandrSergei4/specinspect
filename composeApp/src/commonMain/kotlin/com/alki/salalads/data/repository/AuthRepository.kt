package com.alki.salalads.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Данные авторизованного пользователя
 * Абстракция над FirebaseUser для кроссплатформенности
 */
data class AuthUser(
    val uid: String
)

/**
 * Репозиторий для работы с авторизацией
 * expect класс - реализация в mobileMain (Firebase) и wasmMain (другая)
 */
expect class AuthRepository() {
    val currentUser: AuthUser?
    val currentUserId: String?
    val isLoggedIn: Boolean
    val authStateFlow: Flow<AuthUser?>
    val isLoggedInFlow: Flow<Boolean>

    /**
     * Анонимный вход
     */
    suspend fun signInAnonymously(): Result<AuthUser>

    /**
     * Выход из аккаунта
     */
    suspend fun signOut()
}
