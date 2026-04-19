package com.alki.specinspect.features.review.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual object PlatformStorage {
    private val stateFile: File by lazy {
        val appDir = File(System.getProperty("user.home"), ".specinspect")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        File(appDir, "review-state.json")
    }

    actual suspend fun readState(): String? = withContext(Dispatchers.IO) {
        if (!stateFile.exists()) return@withContext null
        stateFile.readText().ifBlank { null }
    }

    actual suspend fun writeState(serialized: String) {
        withContext(Dispatchers.IO) {
            stateFile.parentFile?.mkdirs()
            stateFile.writeText(serialized)
        }
    }
}

