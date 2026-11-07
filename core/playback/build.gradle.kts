plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.device.test)
}

// Player de áudio atrás de uma porta comum (AudioPlayer). Media3 no Android, AVPlayer no iOS.
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.session)
        }
    }
}
