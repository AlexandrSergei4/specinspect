package com.alki.specinspect.data.storage

import android.content.Context

class AndroidReviewPersistentStorage(
    context: Context,
) : ReviewPersistentStorage {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString(REVIEWS_KEY, null)

    override fun write(value: String) {
        preferences.edit().putString(REVIEWS_KEY, value).apply()
    }

    override fun clear() {
        preferences.edit().remove(REVIEWS_KEY).apply()
    }
}

private const val PREFERENCES_NAME = "specinspect.storage"
private const val REVIEWS_KEY = "persisted_reviews"
