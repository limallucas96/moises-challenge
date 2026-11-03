plugins {
    alias(libs.plugins.convention.kmp.library)
}

// Só dependências e helpers de teste. Fakes de interfaces de feature ficam no
// commonTest da própria feature (skill android-unit-testing: fakes over mocks).
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.domain)
            api(libs.bundles.testing)
        }
        // Só o Android tem teste de UI em aparelho: o helper lê Compose Resources.
        androidMain.dependencies {
            implementation(libs.compose.components.resources)
        }
    }
}
