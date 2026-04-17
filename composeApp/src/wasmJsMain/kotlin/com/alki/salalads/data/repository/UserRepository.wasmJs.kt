@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.alki.salalads.data.repository

import com.alki.salalads.data.models.User
import com.alki.salalads.firebase.FirebaseFirestore
import com.alki.salalads.firebase.docGet
import com.alki.salalads.firebase.docSet
import com.alki.salalads.firebase.firestoreDoc
import com.alki.salalads.firebase.getFirestore
import com.alki.salalads.firebase.jsObjectCreate
import com.alki.salalads.firebase.jsObjectGet
import com.alki.salalads.firebase.jsObjectSet
import com.alki.salalads.firebase.snapshotData
import com.alki.salalads.firebase.snapshotExists
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
import kotlin.time.ExperimentalTime

/**
 * Реализация UserRepository для WASM (браузер)
 * Использует Firebase Firestore JS SDK
 */
@OptIn(ExperimentalTime::class)
actual class UserRepository actual constructor(
    private val authRepository: AuthRepository
) {
    private val firestore: FirebaseFirestore = getFirestore()

    // StateFlows для реактивного обновления

    actual fun getCurrentUserId(): String =
        authRepository.currentUserId ?: throw IllegalStateException("User not authenticated")

    private fun currentUserId(): String = getCurrentUserId()

    private fun userDocPath(userId: String = currentUserId()): String = "users/$userId"

    // ============ USER PROFILE ============

    actual suspend fun createUser(username: String, avatar: String): Result<User> {
        return try {
            val userId = currentUserId()
            val user = User(
                username = username,
                avatar = avatar,
                shareId = userId,
            )

            val data = jsObjectCreate()
            jsObjectSet(data, "username", username)
            jsObjectSet(data, "avatar", avatar)
            jsObjectSet(data, "shareId", userId)

            val docRef = firestoreDoc(firestore, userDocPath())
            awaitPromise(docSet(docRef, data))
            Result.success(user)
        } catch (e: Exception) {
            println("Failed createUser(): ${e.message}")
            Result.failure(e)
        }
    }

    actual suspend fun getCurrentUser(): User? {
        return try {
            val docRef = firestoreDoc(firestore, userDocPath())
            val doc = awaitPromise(docGet(docRef))
            if (doc != null && snapshotExists(doc)) {
                val data = snapshotData(doc)
                if (data != null) parseUser(data) else null
            } else null
        } catch (e: Exception) {
            println("Failed getCurrentUser(): ${e.message}")
            null
        }
    }

    actual suspend fun userExists(): Boolean {
        return try {
            val docRef = firestoreDoc(firestore, userDocPath())
            val doc = awaitPromise(docGet(docRef))
            doc != null && snapshotExists(doc)
        } catch (e: Exception) {
            println("Failed userExists(): ${e.message}")
            false
        }
    }

    actual suspend fun getUserByShareId(shareId: String): Pair<String, User>? {
        return try {
            val docRef = firestoreDoc(firestore, "users/$shareId")
            val doc = awaitPromise(docGet(docRef))
            if (doc != null && snapshotExists(doc)) {
                val data = snapshotData(doc)
                if (data != null) Pair(shareId, parseUser(data)) else null
            } else null
        } catch (e: Exception) {
            println("Failed getUserByShareId(): ${e.message}")
            null
        }
    }

    private fun parseUser(data: kotlin.js.JsAny): User {
        return User(
            username = jsObjectGet(data, "username") ?: "",
            avatar = jsObjectGet(data, "avatar") ?: "",
            shareId = jsObjectGet(data, "shareId") ?: "",
        )
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
