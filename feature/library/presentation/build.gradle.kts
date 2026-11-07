plugins {
    alias(libs.plugins.convention.cmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.library.domain)
            implementation(libs.androidx.paging.compose)
            implementation(libs.coil.compose)
        }
    }
}

compose.resources {
    packageOfResClass = "com.lucaslima.musicsearch.feature.library.presentation.resources"
}
