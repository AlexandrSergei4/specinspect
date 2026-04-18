package com.alki.specinspect.data.repository

import co.touchlab.kermit.Logger
import com.alki.specinspect.data.models.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Реализация UserRepository на основе Firebase Firestore (gitlive)
 * Для мобильных платформ (Android + iOS)
 */
@OptIn(ExperimentalTime::class)
actual class UserRepository actual constructor(
    private val authRepository: AuthRepository
) {
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val usersCollection = firestore.collection("users")

    /**
     * Получить текущий ID пользователя
     */
    actual fun getCurrentUserId(): String =
        authRepository.currentUserId ?: throw IllegalStateException("User not authenticated")

    private fun currentUserId(): String = getCurrentUserId()

    private fun userDoc(userId: String = currentUserId()) = usersCollection.document(userId)

    // ============ USER PROFILE ============

    /**
     * Создать нового пользователя
     */
    actual suspend fun createUser(username: String, avatar: String): Result<User> {
        return try {
            val userId = currentUserId()
            val user = User(
                username = username,
                avatar = avatar,
                shareId = userId,
            )
            userDoc().set(
                mapOf(
                    "username" to username,
                    "avatar" to avatar,
                    "shareId" to userId,
                )
            )
            Result.success(user)
        } catch (e: Exception) {
            Logger.e("Failed createUser()", e)
            Result.failure(e)
        }
    }

    /**
     * Получить текущего пользователя
     */
    actual suspend fun getCurrentUser(): User? {
        return try {
            val doc = userDoc().get()
            if (doc.exists) {
                User(
                    username = doc.get("username"),
                    avatar = doc.get("avatar"),
                    shareId = doc.get("shareId"),
                )
            } else null
        } catch (e: Exception) {
            Logger.e("Failed getCurrentUser()", e)
            null
        }
    }

    /**
     * Проверить существует ли пользователь
     */
    actual suspend fun userExists(): Boolean {
        return try {
            userDoc().get().exists
        } catch (e: Exception) {
            Logger.e("Failed userExists()", e)
            false
        }
    }

    /**
     * Получить пользователя по shareId (для добавления в друзья)
     */
    actual suspend fun getUserByShareId(shareId: String): Pair<String, User>? {
        return try {
            val doc = usersCollection.document(shareId).get()
            if (doc.exists) {
                Pair(
                    doc.id,
                    User(
                        username = doc.get("username"),
                        avatar = doc.get("avatar"),
                        shareId = doc.get("shareId"),
                    )
                )
            } else null
        } catch (e: Exception) {
            Logger.e("Failed getUserByShareId()", e)
            null
        }
    }
}
