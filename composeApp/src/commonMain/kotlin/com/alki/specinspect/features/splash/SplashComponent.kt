package com.alki.specinspect.features.splash

import co.touchlab.kermit.Logger
import com.arkivanov.decompose.ComponentContext
import com.alki.specinspect.data.repository.AuthRepository
import com.alki.specinspect.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Пример реализации фичи - SplashComponent
 */
interface SplashComponent {
    val state: StateFlow<SplashState>
    fun refresh()
}

/**
 * Состояние Splash экрана
 */
data class SplashState(
    val isLoading: Boolean = true
)

/**
 * Реализация Splash компонента
 */
class DefaultSplashComponent(
    componentContext: ComponentContext,
//    private val onNavigateToOnboarding: () -> Unit,
) : SplashComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(SplashState())
    override val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
//        checkAuthState()
    }

    private fun checkAuthState() {
//        onNavigateToOnboarding()
    }

    override fun refresh() {
        scope.launch {
            _state.emit(SplashState())
        }
    }
}