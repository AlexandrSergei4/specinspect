package com.alki.specinspect

import androidx.compose.ui.window.ComposeUIViewController
import com.alki.specinspect.data.analytics.FirebaseAnalyticsLogger
import com.alki.specinspect.data.importer.GitHubContentsSpecificationImporter
import com.alki.specinspect.data.repository.ReviewRepository
import com.alki.specinspect.data.repository.SpecificationRepository
import com.alki.specinspect.data.storage.IosThemePreferenceStorage
import com.alki.specinspect.data.storage.IosUserAccessTokenSecureStorage
import com.alki.specinspect.di.appModule
import com.alki.specinspect.di.platformModule
import com.alki.specinspect.navigation.RootComponent
import com.alki.specinspect.navigation.RootContent
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun MainViewController() = ComposeUIViewController {
    if (KoinPlatform.getKoinOrNull() == null) {
        startKoin { modules(appModule, platformModule) }
    }
    val koin = KoinPlatform.getKoin()
    val specRepo = koin.get<SpecificationRepository>()
    val reviewRepo = koin.get<ReviewRepository>()

    val lifecycle = LifecycleRegistry()
    val rootComponent = RootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        specRepo = specRepo,
        reviewRepo = reviewRepo,
        gitSpecificationImporter = GitHubContentsSpecificationImporter(),
        tokenStorage = IosUserAccessTokenSecureStorage(),
        themePreferenceStorage = IosThemePreferenceStorage(),
        analyticsLogger = FirebaseAnalyticsLogger(),
    )

    RootContent(component = rootComponent)
}
