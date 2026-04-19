package com.alki.specinspect.features.review.platform

import platform.Foundation.NSUserDefaults

actual object PlatformStorage {
    private const val StateKey = "review-state"

    actual suspend fun readState(): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(StateKey)

    actual suspend fun writeState(serialized: String) {
        NSUserDefaults.standardUserDefaults.setObject(serialized, forKey = StateKey)
    }
}
