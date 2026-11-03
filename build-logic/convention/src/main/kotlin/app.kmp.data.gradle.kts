import org.gradle.api.artifacts.VersionCatalogsExtension

/*
 * Módulos data (e :core:data): serialização, Ktor e Koin.
 * Engines do Ktor (OkHttp/Darwin) NÃO entram aqui: só em :core:data, por plataforma.
 */
plugins {
    id("app.kmp.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findBundle("ktor").get())
            implementation(libs.findLibrary("kotlinx-serialization-json").get())
            implementation(libs.findLibrary("koin-core").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("ktor-client-mock").get())
        }
    }
}
