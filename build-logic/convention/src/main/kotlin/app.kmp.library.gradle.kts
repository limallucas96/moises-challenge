import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * Base de TODO módulo KMP: targets Android + iOS, namespace derivado do path,
 * SDK levels, flags de compilador, coroutines e dependências de teste.
 * Sem Compose e sem serialização: é o plugin dos módulos domain.
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("app.module.rules")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

// :feature:auth:data -> com.lucaslima.musicsearch.feature.auth.data
val moduleNamespace = "com.lucaslima.musicsearch." + path.removePrefix(":").replace(":", ".").replace("-", "")

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = moduleNamespace
        compileSdk = libs.findVersion("android-compile-sdk").get().toString().toInt()
        minSdk = libs.findVersion("android-min-sdk").get().toString().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }

        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        optIn.addAll(
            "kotlin.time.ExperimentalTime",
            "kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlin-test").get())
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
            implementation(libs.findLibrary("turbine").get())
            implementation(libs.findLibrary("assertk").get())
        }
    }
}
