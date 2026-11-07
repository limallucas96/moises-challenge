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
include(":core:playback")

// --- Features (domain / data / presentation) -------------------------------
// Library: busca, recentes, player e álbum (as telas "Library - *" do Figma).
include(":feature:library:domain")
include(":feature:library:data")
include(":feature:library:presentation")

// --- Testes de arquitetura -------------------------------------------------
include(":konsist")
