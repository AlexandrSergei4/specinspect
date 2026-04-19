package com.alki.specinspect

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.alki.specinspect.navigation.RootComponent
import com.alki.specinspect.navigation.RootContent

fun MainViewController() = ComposeUIViewController {
    val lifecycle = LifecycleRegistry()

    val rootComponent = RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
    )

    RootContent(component = rootComponent)
}
