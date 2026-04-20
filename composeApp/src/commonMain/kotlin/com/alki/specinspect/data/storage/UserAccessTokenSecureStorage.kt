package com.alki.specinspect.data.storage

interface UserAccessTokenSecureStorage {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clearToken()
}

object NoOpUserAccessTokenSecureStorage : UserAccessTokenSecureStorage {
    override fun getToken(): String? = null

    override fun saveToken(token: String) = Unit

    override fun clearToken() = Unit
}
