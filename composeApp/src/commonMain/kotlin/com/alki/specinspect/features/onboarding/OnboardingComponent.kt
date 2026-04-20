package com.alki.specinspect.features.onboarding

import com.arkivanov.decompose.ComponentContext

interface OnboardingComponent {
    fun onStartDemo()
    fun onOpenMySpecifications()
}

class DefaultOnboardingComponent(
    componentContext: ComponentContext,
    private val onStartDemoCallback: () -> Unit,
    private val onOpenMySpecsCallback: () -> Unit,
) : OnboardingComponent, ComponentContext by componentContext {
    override fun onStartDemo() = onStartDemoCallback()
    override fun onOpenMySpecifications() = onOpenMySpecsCallback()
}
