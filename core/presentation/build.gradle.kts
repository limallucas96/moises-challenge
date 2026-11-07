plugins {
    alias(libs.plugins.convention.cmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.domain)
            // NavKeyRegistration expõe NavKey e KSerializer na API pública.
            api(libs.androidx.navigation3.ui)
            api(libs.kotlinx.serialization.json)
        }
    }
}

compose.resources {
    packageOfResClass = "com.lucaslima.musicsearch.core.presentation.resources"
}
