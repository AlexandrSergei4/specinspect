package com.alki.specinspect.features.onboarding

import com.arkivanov.decompose.ComponentContext

interface OnboardingComponent {
    fun onStartDemo()
    fun onOpenMySpecifications()
    fun onOpenSettings()
}

class DefaultOnboardingComponent(
    componentContext: ComponentContext,
    private val onStartDemoCallback: () -> Unit,
    private val onOpenMySpecsCallback: () -> Unit,
    private val onOpenSettingsCallback: () -> Unit,
) : OnboardingComponent, ComponentContext by componentContext {
    override fun onStartDemo() = onStartDemoCallback()
    override fun onOpenMySpecifications() = onOpenMySpecsCallback()
    override fun onOpenSettings() = onOpenSettingsCallback()
}
