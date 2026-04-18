package com.alki.specinspect

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.alki.specinspect.data.repository.AnswerChecker
import com.alki.specinspect.data.repository.AuthRepository
import com.alki.specinspect.data.repository.StaticDataRepository
import com.alki.specinspect.data.repository.UserRepository
import com.alki.specinspect.di.appModule
import com.alki.specinspect.navigation.RootComponent
import com.alki.specinspect.navigation.RootContent
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun MainViewController() = ComposeUIViewController {
    // Инициализируем Koin если ещё не инициализирован
    if (KoinPlatform.getKoinOrNull() == null) {
        startKoin {
            modules(appModule)
        }
    }

    val koin = KoinPlatform.getKoin()
    val authRepository = koin.get<AuthRepository>()
    val userRepository = koin.get<UserRepository>()
    val staticDataRepository = koin.get<StaticDataRepository>()
    val answerChecker = koin.get<AnswerChecker>()

    // Создаём lifecycle для iOS
    val lifecycle = LifecycleRegistry()

    // Создаём Root компонент
    val rootComponent = RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        authRepository = authRepository,
        userRepository = userRepository,
        staticDataRepository = staticDataRepository,
        answerChecker = answerChecker
    )

    RootContent(component = rootComponent)
}