package com.alki.specinspect.di

import com.alki.specinspect.data.repository.ReviewRepository
import com.alki.specinspect.data.repository.SpecificationRepository
import org.koin.dsl.module

/**
 * Основной Koin модуль приложения.
 * SpecInspect использует in-memory репозитории — состояние теряется при выходе.
 */
val appModule = module {
    single { SpecificationRepository() }
    single { ReviewRepository() }
}
