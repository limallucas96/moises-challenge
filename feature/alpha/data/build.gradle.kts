plugins {
    alias(libs.plugins.convention.kmp.data)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.feature.alpha.domain)
        }
    }
}
