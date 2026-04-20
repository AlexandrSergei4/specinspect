package com.alki.specinspect.data.storage

import android.content.Context

class AndroidSpecificationPersistentStorage(
    context: Context,
) : SpecificationPersistentStorage {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString(SPECS_KEY, null)

    override fun write(value: String) {
        preferences.edit().putString(SPECS_KEY, value).apply()
    }

    override fun clear() {
        preferences.edit().remove(SPECS_KEY).apply()
    }
}

private const val PREFERENCES_NAME = "specinspect.storage"
private const val SPECS_KEY = "persisted_specifications"
