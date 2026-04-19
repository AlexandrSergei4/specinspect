package com.alki.specinspect

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.alki.specinspect.navigation.RootComponent
import com.alki.specinspect.navigation.RootContent
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SpecInspect"
    ) {
        val lifecycle = LifecycleRegistry()
        val rootComponent = RootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle)
        )
        RootContent(component = rootComponent)
    }
}

