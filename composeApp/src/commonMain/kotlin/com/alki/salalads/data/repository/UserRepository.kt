package com.alki.salalads.data.repository

import com.alki.salalads.data.models.User

/**
 * Репозиторий для работы с пользовательскими данными
 * expect класс - реализация в mobileMain (Firebase) и wasmMain (другая)
 */
expect class UserRepository(authRepository: AuthRepository) {
    /**
     * Получить текущий ID пользователя
     */
    fun getCurrentUserId(): String

    // ============ USER PROFILE ============

    /**
     * Создать нового пользователя
     */
    suspend fun createUser(username: String, avatar: String): Result<User>

    /**
     * Получить текущего пользователя
     */
    suspend fun getCurrentUser(): User?

    /**
     * Проверить существует ли пользователь
     */
    suspend fun userExists(): Boolean

    /**
     * Получить пользователя по shareId (для добавления в друзья)
     */
    suspend fun getUserByShareId(shareId: String): Pair<String, User>?

   }
