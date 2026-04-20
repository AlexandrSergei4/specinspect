package com.alki.specinspect.di

import com.alki.specinspect.data.repository.ReviewRepository
import com.alki.specinspect.data.repository.SpecificationRepository
import com.alki.specinspect.data.storage.ReviewPersistentStorage
import com.alki.specinspect.data.storage.SpecificationPersistentStorage
import org.koin.dsl.module

/**
 * Основной Koin модуль приложения.
 */
val appModule = module {
    single { SpecificationRepository(get<SpecificationPersistentStorage>()) }
    single { ReviewRepository(get<ReviewPersistentStorage>()) }
}
