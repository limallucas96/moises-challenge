plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            // paging-common é Kotlin puro (KMP, sem Android): o contrato do repositório expõe PagingData.
            api(libs.androidx.paging.common)
        }
    }
}
