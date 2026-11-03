import org.gradle.api.artifacts.VersionCatalogsExtension

/*
 * Módulos com Composables que não são feature (:core:presentation, :core:designsystem, :shared).
 * O ui-tooling entra no androidRuntimeClasspath porque módulo KMP não tem debugImplementation:
 * é de lá que o Android Studio (AGP 9+) carrega o preview de @Preview em commonMain. Sem ele,
 * o preview falha com ClassNotFoundException: ComposeViewAdapter. Ver
 * https://kotlinlang.org/docs/multiplatform/compose-previews.html
 */
plugins {
    id("app.kmp.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-runtime").get())
            implementation(libs.findLibrary("compose-foundation").get())
            implementation(libs.findLibrary("compose-material3").get())
            implementation(libs.findLibrary("compose-ui").get())
            implementation(libs.findLibrary("compose-components-resources").get())
            implementation(libs.findLibrary("compose-ui-tooling-preview").get())
            implementation(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            implementation(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(libs.findLibrary("compose-ui-tooling").get())
}
