package com.alki.specinspect.di

import com.alki.specinspect.data.storage.AndroidReviewPersistentStorage
import com.alki.specinspect.data.storage.AndroidSpecificationPersistentStorage
import com.alki.specinspect.data.storage.ReviewPersistentStorage
import com.alki.specinspect.data.storage.SpecificationPersistentStorage
import org.koin.dsl.module

val platformModule = module {
    single<ReviewPersistentStorage> { AndroidReviewPersistentStorage(get()) }
    single<SpecificationPersistentStorage> { AndroidSpecificationPersistentStorage(get()) }
}
