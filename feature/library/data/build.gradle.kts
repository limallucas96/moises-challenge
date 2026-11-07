plugins {
    alias(libs.plugins.convention.kmp.data)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.playback)
            implementation(projects.feature.library.domain)

            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)
            implementation(libs.androidx.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.androidx.paging.testing)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

// O compilador do Room roda uma vez por target.
dependencies {
    listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64").forEach { configuration ->
        add(configuration, libs.androidx.room.compiler)
    }
}
