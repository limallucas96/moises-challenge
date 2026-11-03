import org.gradle.api.artifacts.VersionCatalogsExtension

/*
 * Módulos presentation de feature: Compose + Koin + Navigation 3 + BackHandler + serialização
 * (NavKey) e o kernel compartilhado de UI. Uma feature nunca depende de outra.
 * Toda tela tem teste de UI em aparelho (app.device.test).
 */
plugins {
    id("app.cmp.library")
    id("app.device.test")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:presentation"))
            implementation(project(":core:designsystem"))

            implementation(libs.findBundle("koin-compose").get())
            implementation(libs.findLibrary("androidx-navigation3-ui").get())
            implementation(libs.findLibrary("compose-ui-backhandler").get())
            implementation(libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())
            implementation(libs.findLibrary("kotlinx-serialization-json").get())
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.findLibrary("koin-test").get())
        }
    }
}
