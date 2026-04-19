package com.alki.specinspect.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

actual class AuthRepository actual constructor() {
    private val state = MutableStateFlow<AuthUser?>(AuthUser(uid = "desktop-user"))

    actual val currentUser: AuthUser?
        get() = state.value

    actual val currentUserId: String?
        get() = state.value?.uid

    actual val isLoggedIn: Boolean
        get() = state.value != null

    actual val authStateFlow: Flow<AuthUser?> = state

    actual val isLoggedInFlow: Flow<Boolean> = state.map { it != null }

    actual suspend fun signInAnonymously(): Result<AuthUser> {
        val user = state.value ?: AuthUser(uid = "desktop-user")
        state.value = user
        return Result.success(user)
    }

    actual suspend fun signOut() {
        state.value = null
    }
}

