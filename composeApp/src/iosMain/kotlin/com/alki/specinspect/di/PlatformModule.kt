package com.alki.specinspect.di

import com.alki.specinspect.data.storage.IosReviewPersistentStorage
import com.alki.specinspect.data.storage.IosSpecificationPersistentStorage
import com.alki.specinspect.data.storage.ReviewPersistentStorage
import com.alki.specinspect.data.storage.SpecificationPersistentStorage
import org.koin.dsl.module

val platformModule = module {
    single<ReviewPersistentStorage> { IosReviewPersistentStorage() }
    single<SpecificationPersistentStorage> { IosSpecificationPersistentStorage() }
}
