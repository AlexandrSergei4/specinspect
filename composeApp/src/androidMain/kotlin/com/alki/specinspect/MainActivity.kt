package com.alki.specinspect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alki.specinspect.data.repository.ReviewRepository
import com.alki.specinspect.data.repository.SpecificationRepository
import com.alki.specinspect.di.appModule
import com.alki.specinspect.navigation.RootComponent
import com.alki.specinspect.navigation.RootContent
import com.alki.specinspect.util.ClipboardManager
import com.alki.specinspect.util.ImageSharing
import com.alki.specinspect.util.UrlOpener
import com.arkivanov.decompose.defaultComponentContext
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {

    private val specRepo: SpecificationRepository by inject()
    private val reviewRepo: ReviewRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(applicationContext)
                modules(appModule)
            }
        }

        ClipboardManager.init(this)
        ImageSharing.init(this)
        UrlOpener.init(this)

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

        val rootComponent = RootComponent(
            componentContext = defaultComponentContext(),
            specRepo = specRepo,
            reviewRepo = reviewRepo,
        )

        setContent {
            RootContent(component = rootComponent)
        }
    }
}
