package com.alki.specinspect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alki.specinspect.data.repository.AuthRepository
import com.alki.specinspect.di.appModule
import com.alki.specinspect.navigation.RootComponent
import com.alki.specinspect.navigation.RootContent
import com.alki.specinspect.util.ClipboardManager
import com.alki.specinspect.util.ImageSharing
import com.alki.specinspect.util.UrlOpener
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

@OptIn(ExperimentalDecomposeApi::class)
class MainActivity : ComponentActivity() {

    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем Koin если ещё не инициализирован
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(applicationContext)
                modules(appModule)
            }
        }

        // Инициализируем ClipboardManager, ImageSharing и UrlOpener
        ClipboardManager.init(this)
        ImageSharing.init(this)
        UrlOpener.init(this)

        // Настраиваем edge-to-edge режим с темными иконками для светлого фона
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // Создаём Root компонент с Decompose
        val rootComponent = RootComponent(
            componentContext = defaultComponentContext(),
        )

        setContent {
            RootContent(component = rootComponent)
        }
    }
}