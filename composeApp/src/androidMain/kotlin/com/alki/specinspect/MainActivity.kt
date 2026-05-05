package com.alki.specinspect

import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowInsetsControllerCompat
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
                configureEdgeToEdge(useDarkSystemBars)
            }

            RootContent(component = rootComponent)
        }
    }

    @Suppress("DEPRECATION")
    private fun configureEdgeToEdge(useDarkSystemBars: Boolean) {
        val decorView = window.decorView
        decorView.systemUiVisibility = decorView.systemUiVisibility or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.R..34) {
            window.invokeWindowMethod("setDecorFitsSystemWindows", Boolean::class.javaPrimitiveType, false)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.invokeWindowMethod("setStatusBarColor", Int::class.javaPrimitiveType, AndroidColor.TRANSPARENT)
            window.invokeWindowMethod("setNavigationBarColor", Int::class.javaPrimitiveType, AndroidColor.TRANSPARENT)
        }

        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.P..34) {
            val layoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode = layoutMode
            }
        }

        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.Q..34) {
            window.invokeWindowMethod("setStatusBarContrastEnforced", Boolean::class.javaPrimitiveType, false)
            window.invokeWindowMethod("setNavigationBarContrastEnforced", Boolean::class.javaPrimitiveType, false)
        }

        WindowInsetsControllerCompat(window, decorView).run {
            isAppearanceLightStatusBars = !useDarkSystemBars
            isAppearanceLightNavigationBars = !useDarkSystemBars
        }
    }

    private fun Window.invokeWindowMethod(methodName: String, parameterType: Class<*>?, argument: Any) {
        runCatching {
            javaClass.getMethod(methodName, parameterType).invoke(this, argument)
        }
    }
}
