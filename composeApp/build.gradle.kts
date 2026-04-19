import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.ksp)
//  alias(libs.plugins.androidx.room)
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.kotlin.serialization)

}

kotlin {
    jvm("desktop")

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // WASM таргет для веб-приложения
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "kmpapp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        // Промежуточный source set для мобильных платформ (Android + iOS)
        // Содержит actual реализации репозиториев на основе dev.gitlive.firebase
        val mobileMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                // Firebase только для мобильных платформ
                implementation(libs.firebase.auth)
                implementation(libs.firebase.firestore)
                implementation(libs.firebase.functions)
                // SQLite только для мобильных платформ
                implementation(libs.androidx.sqlite.bundled)
            }
        }
        androidMain {
            dependsOn(mobileMain)
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.kotlinx.coroutines.android)
                // Koin Android
                implementation(libs.koin.android)
                // Decompose Android extensions
                implementation(libs.decompose.extensions.android)
                // Coil Ktor network (OkHttp for Android)
                implementation(libs.ktor.client.okhttp)
                implementation(project.dependencies.platform(libs.firebase.bom))
            }
        }
        // iOS промежуточный source set (для actual реализаций Platform, ClipboardManager и т.д.)
        val iosMain by creating {
            dependsOn(mobileMain)
            dependencies {
                // Ktor Darwin клиент для Coil (загрузка изображений)
                implementation(libs.ktor.client.darwin)
            }
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
        // WASM source set (альтернативные реализации репозиториев без Firebase)
        val wasmJsMain by getting {
            dependencies {
                // Ktor JS клиент для Coil (загрузка изображений) и HTTP запросов
                implementation(libs.ktor.client.js)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.okhttp)
            }
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.coroutines.core)
//            implementation(libs.androidx.room.runtime)
            // sqlite-bundled перемещен в mobileMain (не поддерживает WASM)
            implementation(libs.compose.icons.tabler)
            // Decompose
            implementation(libs.decompose)
            implementation(libs.decompose.extensions.compose)
            // Essenty
            implementation(libs.essenty.lifecycle)
            implementation(libs.essenty.state.keeper)
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            // kotlinx-datetime
            implementation(libs.kotlinx.datetime)
            // kotlinx-serialization
            implementation(libs.kotlinx.serialization.json)
            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.ktor.client.core)
            implementation(libs.kermit)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.alki.specinspect"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.alki.specinspect"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
//    add("kspAndroid", libs.androidx.room.compiler)
//    add("kspJvm", libs.androidx.room.compiler)
//    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
//    add("kspIosArm64", libs.androidx.room.compiler)
}

//room {
//    schemaDirectory("$projectDir/schemas")
//}

compose.desktop {
    application {
        mainClass = "com.alki.specinspect.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.alki.specinspect"
            packageVersion = "1.0.0"
        }
    }
}
