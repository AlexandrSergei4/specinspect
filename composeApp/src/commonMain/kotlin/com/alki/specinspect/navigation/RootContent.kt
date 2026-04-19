package com.alki.specinspect.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alki.specinspect.features.addspec.AddSpecScreen
import com.alki.specinspect.features.myspecs.MySpecsScreen
import com.alki.specinspect.features.onboarding.OnboardingScreen
import com.alki.specinspect.features.requirement.RequirementDetailScreen
import com.alki.specinspect.features.review.ScenarioReviewScreen
import com.alki.specinspect.features.spec.SpecDetailScreen
import com.alki.specinspect.features.subspec.SubspecDetailScreen
import com.alki.specinspect.ui.theme.SampleTheme
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    SampleTheme {
        Children(
            stack = component.childStack,
            modifier = modifier.fillMaxSize(),
            animation = stackAnimation(fade() + slide()),
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.Onboarding -> OnboardingScreen(instance.component)
                is RootComponent.Child.MySpecs -> MySpecsScreen(instance.component)
                is RootComponent.Child.SpecDetail -> SpecDetailScreen(instance.component)
                is RootComponent.Child.SubspecDetail -> SubspecDetailScreen(instance.component)
                is RootComponent.Child.RequirementDetail -> RequirementDetailScreen(instance.component)
                is RootComponent.Child.Review -> ScenarioReviewScreen(instance.component)
                is RootComponent.Child.AddSpec -> AddSpecScreen(instance.component)
            }
        }
    }
}
