package com.alki.specinspect.data.storage

import com.alki.specinspect.ui.theme.AppThemeMode

interface ThemePreferenceStorage {
    fun getThemeMode(): AppThemeMode
    fun setThemeMode(mode: AppThemeMode)
}

object NoOpThemePreferenceStorage : ThemePreferenceStorage {
    private var mode: AppThemeMode = AppThemeMode.System

    override fun getThemeMode(): AppThemeMode = mode

    override fun setThemeMode(mode: AppThemeMode) {
        this.mode = mode
    }
}
