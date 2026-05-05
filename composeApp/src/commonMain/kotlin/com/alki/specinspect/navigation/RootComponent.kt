package com.alki.specinspect.navigation

import com.alki.specinspect.data.analytics.AnalyticsLogger
import com.alki.specinspect.data.analytics.NoOpAnalyticsLogger
import com.alki.specinspect.data.importer.GitSpecificationImporter
import com.alki.specinspect.data.importer.UnsupportedGitSpecificationImporter
import com.alki.specinspect.data.repository.ReviewRepository
import com.alki.specinspect.data.repository.SpecificationRepository
import com.alki.specinspect.data.storage.NoOpUserAccessTokenSecureStorage
import com.alki.specinspect.data.storage.NoOpThemePreferenceStorage
import com.alki.specinspect.data.storage.ThemePreferenceStorage
import com.alki.specinspect.data.storage.UserAccessTokenSecureStorage
import com.alki.specinspect.features.addspec.AddSpecComponent
import com.alki.specinspect.features.addspec.DefaultAddSpecComponent
import com.alki.specinspect.features.myspecs.DefaultMySpecsComponent
import com.alki.specinspect.features.myspecs.MySpecsComponent
import com.alki.specinspect.features.onboarding.DefaultOnboardingComponent
import com.alki.specinspect.features.onboarding.OnboardingComponent
import com.alki.specinspect.features.requirement.DefaultRequirementDetailComponent
import com.alki.specinspect.features.requirement.RequirementDetailComponent
import com.alki.specinspect.features.review.DefaultScenarioReviewComponent
import com.alki.specinspect.features.review.ReviewScope
import com.alki.specinspect.features.review.ScenarioReviewComponent
import com.alki.specinspect.features.settings.DefaultSettingsComponent
import com.alki.specinspect.features.settings.SettingsComponent
import com.alki.specinspect.features.spec.DefaultSpecDetailComponent
import com.alki.specinspect.features.spec.SpecDetailComponent
import com.alki.specinspect.features.subspec.DefaultSubspecDetailComponent
import com.alki.specinspect.features.subspec.SubspecDetailComponent
import com.alki.specinspect.features.webcontent.DefaultWebContentComponent
import com.alki.specinspect.features.webcontent.WebContentComponent
import com.alki.specinspect.ui.theme.AppThemeMode
import com.alki.specinspect.util.UrlOpener
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

class RootComponent(
    componentContext: ComponentContext,
    private val specRepo: SpecificationRepository,
    private val reviewRepo: ReviewRepository,
    private val gitSpecificationImporter: GitSpecificationImporter = UnsupportedGitSpecificationImporter(),
    private val tokenStorage: UserAccessTokenSecureStorage = NoOpUserAccessTokenSecureStorage,
    private val themePreferenceStorage: ThemePreferenceStorage = NoOpThemePreferenceStorage,
    private val analyticsLogger: AnalyticsLogger = NoOpAnalyticsLogger,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()
    private val _themeMode = MutableStateFlow(themePreferenceStorage.getThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Onboarding,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, context: ComponentContext): Child = when (config) {
        is Config.Onboarding -> Child.Onboarding(
            DefaultOnboardingComponent(
                componentContext = context,
                onStartDemoCallback = { navigation.pushNew(Config.SpecDetail(specId = specRepo.getDemo().id)) },
                onOpenMySpecsCallback = { navigation.pushNew(Config.MySpecs) },
                onOpenSettingsCallback = { navigation.pushNew(Config.Settings) },
            )
        )
        is Config.MySpecs -> Child.MySpecs(
            DefaultMySpecsComponent(
                componentContext = context,
                repo = specRepo,
                onAddCallback = { navigation.pushNew(Config.AddSpec) },
                onOpenCallback = { id -> navigation.pushNew(Config.SpecDetail(id)) },
                onBackCallback = { navigation.pop() },
                onOpenSettingsCallback = { navigation.pushNew(Config.Settings) },
            )
        )
        is Config.Settings -> Child.Settings(
            DefaultSettingsComponent(
                componentContext = context,
                themeMode = themeMode,
                onThemeModeSelectedCallback = ::setThemeMode,
                onContactDeveloperCallback = {
                    UrlOpener.openUrl("mailto:paslenapp@gmail.com?subject=SpecInspect%20feedback")
                },
                onOpenPrivacyPolicyCallback = {
                    navigation.pushNew(
                        Config.WebContent(
                            title = "Privacy Policy",
                            url = "https://paslenstudio.github.io/SpecInspect_Policy.html",
                        )
                    )
                },
                onBackCallback = { navigation.pop() },
            )
        )
        is Config.WebContent -> Child.WebContent(
            DefaultWebContentComponent(
                componentContext = context,
                title = config.title,
                url = config.url,
                onBackCallback = { navigation.pop() },
            )
        )
        is Config.SpecDetail -> Child.SpecDetail(
            DefaultSpecDetailComponent(
                componentContext = context,
                specId = config.specId,
                specRepo = specRepo,
                reviewRepo = reviewRepo,
                analyticsLogger = analyticsLogger,
                onBackCallback = { navigation.pop() },
                onStartReviewCallback = { specId ->
                    navigation.pushNew(Config.Review(ReviewScope.Spec(specId)))
                },
                onOpenSubspecCallback = { specId, subspecId ->
                    navigation.pushNew(Config.SubspecDetail(specId, subspecId))
                },
                onOpenRequirementCallback = { specId, subspecId, reqId ->
                    navigation.pushNew(Config.RequirementDetail(specId, subspecId, reqId))
                },
            )
        )
        is Config.SubspecDetail -> Child.SubspecDetail(
            DefaultSubspecDetailComponent(
                componentContext = context,
                specId = config.specId,
                subspecId = config.subspecId,
                specRepo = specRepo,
                reviewRepo = reviewRepo,
                analyticsLogger = analyticsLogger,
                onBackCallback = { navigation.pop() },
                onStartReviewCallback = { specId, subspecId ->
                    navigation.pushNew(Config.Review(ReviewScope.Subspec(specId, subspecId)))
                },
                onOpenRequirementCallback = { specId, subspecId, reqId ->
                    navigation.pushNew(Config.RequirementDetail(specId, subspecId, reqId))
                },
            )
        )
        is Config.RequirementDetail -> Child.RequirementDetail(
            DefaultRequirementDetailComponent(
                componentContext = context,
                specId = config.specId,
                subspecId = config.subspecId,
                requirementId = config.requirementId,
                specRepo = specRepo,
                reviewRepo = reviewRepo,
                analyticsLogger = analyticsLogger,
                onBackCallback = { navigation.pop() },
                onStartReviewCallback = { specId, subspecId, reqId ->
                    navigation.pushNew(Config.Review(ReviewScope.Requirement(specId, subspecId, reqId)))
                },
            )
        )
        is Config.Review -> Child.Review(
            DefaultScenarioReviewComponent(
                componentContext = context,
                scope = config.scope,
                specRepo = specRepo,
                reviewRepo = reviewRepo,
                analyticsLogger = analyticsLogger,
                onBackCallback = { navigation.pop() },
            )
        )
        is Config.AddSpec -> Child.AddSpec(
            DefaultAddSpecComponent(
                componentContext = context,
                repo = specRepo,
                importer = gitSpecificationImporter,
                tokenStorage = tokenStorage,
                analyticsLogger = analyticsLogger,
                onBackCallback = { navigation.pop() },
                onAddedCallback = { navigation.pop() },
            )
        )
    }

    private fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        themePreferenceStorage.setThemeMode(mode)
    }

    @Serializable
    sealed class Config {
        @Serializable data object Onboarding : Config()
        @Serializable data object MySpecs : Config()
        @Serializable data object Settings : Config()
        @Serializable data class WebContent(val title: String, val url: String) : Config()
        @Serializable data class SpecDetail(val specId: String) : Config()
        @Serializable data class SubspecDetail(val specId: String, val subspecId: String) : Config()
        @Serializable data class RequirementDetail(
            val specId: String,
            val subspecId: String,
            val requirementId: String,
        ) : Config()
        @Serializable data class Review(val scope: ReviewScope) : Config()
        @Serializable data object AddSpec : Config()
    }

    sealed class Child {
        data class Onboarding(val component: OnboardingComponent) : Child()
        data class MySpecs(val component: MySpecsComponent) : Child()
        data class Settings(val component: SettingsComponent) : Child()
        data class WebContent(val component: WebContentComponent) : Child()
        data class SpecDetail(val component: SpecDetailComponent) : Child()
        data class SubspecDetail(val component: SubspecDetailComponent) : Child()
        data class RequirementDetail(val component: RequirementDetailComponent) : Child()
        data class Review(val component: ScenarioReviewComponent) : Child()
        data class AddSpec(val component: AddSpecComponent) : Child()
    }
}
