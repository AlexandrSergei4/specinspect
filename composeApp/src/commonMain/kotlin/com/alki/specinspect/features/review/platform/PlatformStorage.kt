package com.alki.specinspect.features.review.platform

expect object PlatformStorage {
    suspend fun readState(): String?
    suspend fun writeState(serialized: String)
}
