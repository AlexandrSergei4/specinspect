package com.alki.specinspect.features.review.platform

import kotlinx.browser.window

actual object PlatformStorage {
    private const val StateKey = "review-state"

    actual suspend fun readState(): String? =
        window.localStorage.getItem(StateKey)

    actual suspend fun writeState(serialized: String) {
        window.localStorage.setItem(StateKey, serialized)
    }
}
