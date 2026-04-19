package com.alki.specinspect.navigation

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

@OptIn(DelicateDecomposeApi::class)
class RootComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()

    val stack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Onboarding,
        handleBackButton = true,
        childFactory = ::child
    )

    fun openOnboarding() = navigation.push(Config.Onboarding)
    fun openLibraries() = navigation.push(Config.MySpecifications)
    fun openAddSpecification() = navigation.push(Config.AddSpecification)
    fun openSpecification(specId: String) = navigation.push(Config.SpecificationDetail(specId))
    fun openSubspec(specId: String, subspecId: String) = navigation.push(Config.SubspecDetail(specId, subspecId))
    fun openRequirement(specId: String, subspecId: String, requirementId: String) =
        navigation.push(Config.RequirementDetail(specId, subspecId, requirementId))

    fun openReview(scope: ReviewScope) = navigation.push(Config.ScenarioReview(scope))
    fun goBack() = navigation.pop()
    fun openLibrariesRoot() = navigation.replaceAll(Config.MySpecifications)

    private fun child(config: Config, componentContext: ComponentContext): Child =
        when (config) {
            is Config.Onboarding -> Child.Onboarding
            is Config.MySpecifications -> Child.MySpecifications
            is Config.AddSpecification -> Child.AddSpecification
            is Config.SpecificationDetail -> Child.SpecificationDetail(config.specId)
            is Config.SubspecDetail -> Child.SubspecDetail(config.specId, config.subspecId)
            is Config.RequirementDetail -> Child.RequirementDetail(
                specId = config.specId,
                subspecId = config.subspecId,
                requirementId = config.requirementId
            )
            is Config.ScenarioReview -> Child.ScenarioReview(config.scope)
        }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Onboarding : Config

        @Serializable
        data object MySpecifications : Config

        @Serializable
        data object AddSpecification : Config

        @Serializable
        data class SpecificationDetail(val specId: String) : Config

        @Serializable
        data class SubspecDetail(val specId: String, val subspecId: String) : Config

        @Serializable
        data class RequirementDetail(
            val specId: String,
            val subspecId: String,
            val requirementId: String
        ) : Config

        @Serializable
        data class ScenarioReview(val scope: ReviewScope) : Config
    }

    sealed interface Child {
        data object Onboarding : Child
        data object MySpecifications : Child
        data object AddSpecification : Child
        data class SpecificationDetail(val specId: String) : Child
        data class SubspecDetail(val specId: String, val subspecId: String) : Child
        data class RequirementDetail(val specId: String, val subspecId: String, val requirementId: String) : Child
        data class ScenarioReview(val scope: ReviewScope) : Child
    }
}

@Serializable
sealed interface ReviewScope {
    @Serializable
    data class Specification(val specId: String) : ReviewScope

    @Serializable
    data class Subspec(val specId: String, val subspecId: String) : ReviewScope

    @Serializable
    data class Requirement(
        val specId: String,
        val subspecId: String,
        val requirementId: String
    ) : ReviewScope
}
