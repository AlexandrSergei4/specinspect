package com.alki.specinspect.data.storage

import com.alki.specinspect.ui.theme.AppThemeMode
import platform.Foundation.NSUserDefaults

class IosThemePreferenceStorage : ThemePreferenceStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getThemeMode(): AppThemeMode =
        AppThemeMode.fromStorageValue(defaults.stringForKey(KEY_THEME_MODE))

    override fun setThemeMode(mode: AppThemeMode) {
        defaults.setObject(mode.storageValue, forKey = KEY_THEME_MODE)
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
