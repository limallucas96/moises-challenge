import org.gradle.api.artifacts.VersionCatalogsExtension

/*
 * Testes instrumentados: liga o source set androidDeviceTest, que roda em aparelho ou
 * emulador com ./gradlew connectedAndroidDeviceTest. O app.cmp.feature aplica este plugin; os
 * outros módulos que têm teste em aparelho aplicam à mão.
 *
 * O androidDeviceTest entra na árvore "test" para herdar o commonTest: é lá que ficam os fakes de
 * cada feature. Como os testes unitários viriam junto, o runner só executa classes com DeviceTest
 * no nome (o :konsist confere). O filtro vai para o shell do aparelho sem aspas: nada de regex com
 * parênteses ou barra vertical.
 */
plugins {
    id("app.kmp.library")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    android {
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            instrumentationRunnerArguments["tests_regex"] = "DeviceTest"
            animationsDisabled = true
        }
    }

    sourceSets {
        getByName("androidDeviceTest").dependencies {
            implementation(libs.findLibrary("androidx-test-runner").get())
        }
    }
}

// Só módulo com Compose precisa das regras de teste de UI e da Activity vazia que hospeda a tela.
pluginManager.withPlugin("org.jetbrains.compose") {
    kotlin.sourceSets.getByName("androidDeviceTest").dependencies {
        implementation(libs.findLibrary("compose-ui-test-junit4").get())
        implementation(libs.findLibrary("androidx-compose-ui-test-manifest").get())
        // O Compose espera a tela ficar ociosa pelo Espresso; a versão que ele traz é antiga.
        implementation(libs.findLibrary("androidx-test-espresso-core").get())
    }
}
