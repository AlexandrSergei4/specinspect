package com.alki.specinspect.di

import org.koin.dsl.module

/**
 * Основной Koin модуль приложения
 * Репозитории используют expect/actual паттерн:
 * - commonMain содержит expect декларации
 * - mobileMain содержит actual реализации с Firebase
 * - wasmJsMain содержит реализации с localStorage
 */
val appModule = module {
    // Repositories (actual реализации подставляются через expect/actual механизм)
//    single { SomeRepository }
}