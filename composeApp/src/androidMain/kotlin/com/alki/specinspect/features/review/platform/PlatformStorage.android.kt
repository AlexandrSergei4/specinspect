package com.alki.specinspect.features.review.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual object PlatformStorage {
    private const val PrefsName = "specinspect-review"
    private const val StateKey = "review-state"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual suspend fun readState(): String? = withContext(Dispatchers.IO) {
        appContext
            ?.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
            ?.getString(StateKey, null)
    }

    actual suspend fun writeState(serialized: String) {
        withContext(Dispatchers.IO) {
            appContext
                ?.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
                ?.edit()
                ?.putString(StateKey, serialized)
                ?.apply()
        }
    }
}
