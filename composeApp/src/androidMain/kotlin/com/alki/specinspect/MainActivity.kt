package com.alki.specinspect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alki.specinspect.features.review.platform.PlatformFolderImporter
import com.alki.specinspect.features.review.platform.PlatformStorage
import com.alki.specinspect.navigation.RootComponent
import com.alki.specinspect.navigation.RootContent
import com.alki.specinspect.util.ClipboardManager
import com.alki.specinspect.util.ImageSharing
import com.alki.specinspect.util.UrlOpener
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext

@OptIn(ExperimentalDecomposeApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализируем ClipboardManager, ImageSharing и UrlOpener
        ClipboardManager.init(this)
        ImageSharing.init(this)
        UrlOpener.init(this)
        PlatformStorage.init(this)
        PlatformFolderImporter.init(this)

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
        )

        setContent {
            RootContent(component = rootComponent)
        }
    }
}
