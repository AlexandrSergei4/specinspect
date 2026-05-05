package com.alki.specinspect.features.webcontent

import com.arkivanov.decompose.ComponentContext

interface WebContentComponent {
    val title: String
    val url: String
    fun onBack()
}

class DefaultWebContentComponent(
    componentContext: ComponentContext,
    override val title: String,
    override val url: String,
    private val onBackCallback: () -> Unit,
) : WebContentComponent, ComponentContext by componentContext {
    override fun onBack() = onBackCallback()
}
