package com.alki.salalads.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.alki.salalads.features.splash.SplashScreen
import com.alki.salalads.ui.theme.SampleTheme
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

/**
 * Корневой Composable для отображения всех экранов
 */
@OptIn(ExperimentalCoilApi::class)
@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier
) {
    // Настройка Coil ImageLoader с Ktor для корректной загрузки изображений на всех платформах
    setSingletonImageLoaderFactory { context: PlatformContext ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }

    SampleTheme {
        Children(
            stack = component.childStack,
            modifier = modifier.fillMaxSize(),
            animation = stackAnimation(fade() + scale())
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.Splash -> SplashScreen(instance.component)
            }
        }
    }
}