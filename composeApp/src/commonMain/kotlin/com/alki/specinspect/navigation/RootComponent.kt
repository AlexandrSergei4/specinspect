package com.alki.specinspect.navigation

import com.alki.specinspect.data.repository.ReviewRepository
import com.alki.specinspect.data.repository.SpecificationRepository
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
import com.alki.specinspect.features.spec.DefaultSpecDetailComponent
import com.alki.specinspect.features.spec.SpecDetailComponent
import com.alki.specinspect.features.subspec.DefaultSubspecDetailComponent
import com.alki.specinspect.features.subspec.SubspecDetailComponent
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

class RootComponent(
    componentContext: ComponentContext,
    private val specRepo: SpecificationRepository,
    private val reviewRepo: ReviewRepository,
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

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
            )
        )
        is Config.MySpecs -> Child.MySpecs(
            DefaultMySpecsComponent(
                componentContext = context,
                repo = specRepo,
                onAddCallback = { navigation.pushNew(Config.AddSpec) },
                onOpenCallback = { id -> navigation.pushNew(Config.SpecDetail(id)) },
                onBackCallback = { navigation.pop() },
            )
        )
        is Config.SpecDetail -> Child.SpecDetail(
            DefaultSpecDetailComponent(
                componentContext = context,
                specId = config.specId,
                specRepo = specRepo,
                reviewRepo = reviewRepo,
                onBackCallback = { navigation.pop() },
                onStartReviewCallback = { specId ->
                    navigation.pushNew(Config.Review(ReviewScope.Spec(specId)))
                },
                onOpenSubspecCallback = { specId, subspecId ->
                    navigation.pushNew(Config.SubspecDetail(specId, subspecId))
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
                onBackCallback = { navigation.pop() },
            )
        )
        is Config.AddSpec -> Child.AddSpec(
            DefaultAddSpecComponent(
                componentContext = context,
                repo = specRepo,
                onBackCallback = { navigation.pop() },
                onAddedCallback = { navigation.pop() },
            )
        )
    }

    @Serializable
    sealed class Config {
        @Serializable data object Onboarding : Config()
        @Serializable data object MySpecs : Config()
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
        data class SpecDetail(val component: SpecDetailComponent) : Child()
        data class SubspecDetail(val component: SubspecDetailComponent) : Child()
        data class RequirementDetail(val component: RequirementDetailComponent) : Child()
        data class Review(val component: ScenarioReviewComponent) : Child()
        data class AddSpec(val component: AddSpecComponent) : Child()
    }
}
