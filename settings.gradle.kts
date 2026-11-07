rootProject.name = "musicsearch"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// --- Entry points e composição ---------------------------------------------
include(":androidApp")
include(":shared")

// --- Core ------------------------------------------------------------------
include(":core:domain")
include(":core:data")
include(":core:presentation")
include(":core:designsystem")
include(":core:testing")

// --- Features (domain / data / presentation) -------------------------------
// Duas features de exemplo, só para exercitar módulos, DI e navegação.
include(":feature:alpha:domain")
include(":feature:alpha:data")
include(":feature:alpha:presentation")

// Só presentation: não tem dados próprios.
include(":feature:beta:presentation")

// --- Testes de arquitetura -------------------------------------------------
include(":konsist")
