import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    // Falha o build se o relógio passar a depender do :shared (ARCHITECTURE.md §2).
    alias(libs.plugins.convention.module.rules)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

// App do relógio: segundo composition root, ao lado do :shared. Reaproveita domain, data,
// player e os ViewModels da feature; só a UI é própria, feita com Wear Compose.
dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.presentation)
    implementation(projects.core.designsystem)
    implementation(projects.core.playback)
    implementation(projects.feature.library.domain)
    implementation(projects.feature.library.data)
    implementation(projects.feature.library.presentation)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.wear.compose.material3)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation3)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.bundles.koin.compose)
    implementation(libs.koin.android)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)

    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.assertk)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

android {
    namespace = "com.lucaslima.musicsearch.wear"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()

    defaultConfig {
        // Mesmo id do app do celular: é assim que a Play Store liga os dois.
        applicationId = "com.lucaslima.musicsearch"
        // Wear OS 3 em diante.
        minSdk = 30
        targetSdk = libs.versions.android.target.sdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}
