plugins {
    alias(libs.plugins.convention.cmp.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            // Sem isso o Kotlin/Native avisa que não consegue inferir o bundle ID do framework.
            binaryOption("bundleId", "cmp.skeleton.app.shared")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Composition root: o único módulo que conhece todas as features.
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.presentation)
            implementation(projects.core.designsystem)

            implementation(projects.feature.alpha.domain)
            implementation(projects.feature.alpha.data)
            implementation(projects.feature.alpha.presentation)

            implementation(projects.feature.beta.presentation)

            implementation(libs.bundles.koin.compose)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
