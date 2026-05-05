package com.alki.specinspect.features.settings

import com.alki.specinspect.ui.theme.AppThemeMode
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.StateFlow

interface SettingsComponent {
    val themeMode: StateFlow<AppThemeMode>
    fun onThemeModeSelected(mode: AppThemeMode)
    fun onBack()
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    override val themeMode: StateFlow<AppThemeMode>,
    private val onThemeModeSelectedCallback: (AppThemeMode) -> Unit,
    private val onBackCallback: () -> Unit,
) : SettingsComponent, ComponentContext by componentContext {
    override fun onThemeModeSelected(mode: AppThemeMode) = onThemeModeSelectedCallback(mode)
    override fun onBack() = onBackCallback()
}
