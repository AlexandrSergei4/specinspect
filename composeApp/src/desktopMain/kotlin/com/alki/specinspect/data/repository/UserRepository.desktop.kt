package com.alki.specinspect.data.repository

import com.alki.specinspect.data.models.User

actual class UserRepository actual constructor(
    private val authRepository: AuthRepository
) {
    private val users = mutableMapOf<String, User>()

    actual fun getCurrentUserId(): String =
        authRepository.currentUserId ?: error("User not authenticated")

    actual suspend fun createUser(username: String, avatar: String): Result<User> {
        val userId = getCurrentUserId()
        val user = User(
            username = username,
            avatar = avatar,
            shareId = userId
        )
        users[userId] = user
        return Result.success(user)
    }

    actual suspend fun getCurrentUser(): User? =
        users[getCurrentUserId()]

    actual suspend fun userExists(): Boolean =
        users.containsKey(getCurrentUserId())

    actual suspend fun getUserByShareId(shareId: String): Pair<String, User>? =
        users[shareId]?.let { shareId to it }
}
