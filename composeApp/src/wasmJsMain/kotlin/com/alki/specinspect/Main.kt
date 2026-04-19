package com.alki.specinspect

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.alki.specinspect.navigation.RootComponent
import com.alki.specinspect.navigation.RootContent
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.webhistory.DefaultWebHistoryController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.browser.document

/**
 * Точка входа для WASM веб-приложения
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalDecomposeApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()

    val webHistoryController = DefaultWebHistoryController()

    val rootComponent = RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
    )

    ComposeViewport(document.body!!) {
        RootContent(component = rootComponent)
    }
}
