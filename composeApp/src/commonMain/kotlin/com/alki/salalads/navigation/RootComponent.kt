package com.alki.salalads.navigation

import co.touchlab.kermit.Logger
import com.alki.salalads.features.splash.DefaultSplashComponent
import com.alki.salalads.features.splash.SplashComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.Serializable

/**
 * Root компонент приложения
 * Управляет основной навигацией между экранами
 */
@OptIn(ExperimentalDecomposeApi::class)
class RootComponent(
    componentContext: ComponentContext,
//    private val someRepository: SomeRepository,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Splash,
        handleBackButton = true,
        childFactory = ::createChild
    )

    /**
     * Вызывает refresh на активном компоненте, если он поддерживает обновление
     * Критично для WASM, где lifecycle.doOnResume может не работать корректно
     */
    private fun refreshActiveComponent(child: Child) {
        Logger.d { "Refreshing active component: ${child::class.simpleName}" }
        when (child) {
            is Child.Splash -> child.component.refresh()
        }
    }

    /**
     * Преобразует конфигурацию в URL path для браузерной истории
     */
    private fun Config.toPath(): String = when (this) {
        is Config.Splash -> "/"
    }

    /**
     * Преобразует URL path обратно в конфигурацию
     */
    private fun String.toConfig(): Config {
        val segments = this.removePrefix("/").split("/")
        return when {
            this == "/" || segments.isEmpty() || segments[0].isEmpty() -> Config.Splash
            else -> Config.Splash
        }
    }

    private fun createChild(config: Config, context: ComponentContext): Child {
        return when (config) {
            is Config.Splash -> Child.Splash(
                DefaultSplashComponent(
                    componentContext = context,
                )
            )
        }
    }

    /**
     * Конфигурации навигации
     */
    @Serializable
    sealed class Config {
        @Serializable
        data object Splash : Config()

       }

    /**
     * Дочерние компоненты
     */
    sealed class Child {
        data class Splash(val component: SplashComponent) : Child()
    }
}