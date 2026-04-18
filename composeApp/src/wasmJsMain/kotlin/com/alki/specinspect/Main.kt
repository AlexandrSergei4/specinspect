package com.alki.specinspect

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.alki.specinspect.data.repository.AuthRepository
import com.alki.specinspect.data.repository.UserRepository
import com.alki.specinspect.di.appModule
import com.alki.specinspect.navigation.RootComponent
import com.alki.specinspect.navigation.RootContent
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.webhistory.DefaultWebHistoryController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.browser.document
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

/**
 * Точка входа для WASM веб-приложения
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalDecomposeApi::class)
fun main() {
    // Инициализируем Koin
    startKoin {
        modules(appModule)
    }

    val koin = KoinPlatform.getKoin()
    val authRepository = koin.get<AuthRepository>()
    val userRepository = koin.get<UserRepository>()

    // Создаем lifecycle для Decompose
    val lifecycle = LifecycleRegistry()

    // Создаем WebHistoryController для обработки кнопки назад в браузере
    val webHistoryController = DefaultWebHistoryController()

    // Создаем корневой компонент
    val rootComponent = RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
    )

    // Запускаем Compose приложение через ComposeViewport
    ComposeViewport(document.body!!) {
        RootContent(component = rootComponent)
    }
}
