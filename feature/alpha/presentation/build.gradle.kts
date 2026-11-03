plugins {
    alias(libs.plugins.convention.cmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.alpha.domain)
        }
    }
}

compose.resources {
    packageOfResClass = "cmp.skeleton.app.feature.alpha.presentation.resources"
}
