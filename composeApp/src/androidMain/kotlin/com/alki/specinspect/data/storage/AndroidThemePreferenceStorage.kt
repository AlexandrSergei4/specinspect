package com.alki.specinspect.data.storage

import android.content.Context
import com.alki.specinspect.ui.theme.AppThemeMode

class AndroidThemePreferenceStorage(context: Context) : ThemePreferenceStorage {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun getThemeMode(): AppThemeMode =
        AppThemeMode.fromStorageValue(preferences.getString(KEY_THEME_MODE, null))

    override fun setThemeMode(mode: AppThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.storageValue).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "specinspect_settings"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
