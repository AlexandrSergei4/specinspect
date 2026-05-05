package com.alki.specinspect

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.alki.specinspect.data.analytics.FirebaseAnalyticsLogger
import com.alki.specinspect.data.importer.GitHubContentsSpecificationImporter
import com.alki.specinspect.data.repository.ReviewRepository
import com.alki.specinspect.data.repository.SpecificationRepository
import com.alki.specinspect.data.storage.AndroidThemePreferenceStorage
import com.alki.specinspect.data.storage.AndroidUserAccessTokenSecureStorage
import com.alki.specinspect.di.appModule
import com.alki.specinspect.di.platformModule
import com.alki.specinspect.navigation.RootComponent
import com.alki.specinspect.navigation.RootContent
import com.alki.specinspect.ui.theme.AppThemeMode
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
                modules(appModule, platformModule)
            }
        }

        ClipboardManager.init(this)
        ImageSharing.init(this)
        UrlOpener.init(this)

        val rootComponent = RootComponent(
            componentContext = defaultComponentContext(),
            specRepo = specRepo,
            reviewRepo = reviewRepo,
            gitSpecificationImporter = GitHubContentsSpecificationImporter(),
            tokenStorage = AndroidUserAccessTokenSecureStorage(applicationContext),
            themePreferenceStorage = AndroidThemePreferenceStorage(applicationContext),
            analyticsLogger = FirebaseAnalyticsLogger(),
        )

        setContent {
            val themeMode by rootComponent.themeMode.collectAsState()
            val systemInDarkTheme = isSystemInDarkTheme()
            val useDarkSystemBars = when (themeMode) {
                AppThemeMode.System -> systemInDarkTheme
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
            }

            SideEffect {
                val systemBarStyle = if (useDarkSystemBars) {
                    SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                } else {
                    SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                }
                enableEdgeToEdge(
                    statusBarStyle = systemBarStyle,
                    navigationBarStyle = systemBarStyle,
                )
            }

            RootContent(component = rootComponent)
        }
    }
}
