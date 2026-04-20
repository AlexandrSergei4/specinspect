package com.alki.specinspect.data.storage

import platform.Foundation.NSUserDefaults

class IosReviewPersistentStorage : ReviewPersistentStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): String? = defaults.stringForKey(REVIEWS_KEY)

    override fun write(value: String) {
        defaults.setObject(value, forKey = REVIEWS_KEY)
    }

    override fun clear() {
        defaults.removeObjectForKey(REVIEWS_KEY)
    }
}

private const val REVIEWS_KEY = "persisted_reviews"
