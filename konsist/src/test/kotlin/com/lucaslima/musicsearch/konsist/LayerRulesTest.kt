package com.lucaslima.musicsearch.konsist

import kotlin.test.Test

/** ARCHITECTURE.md §1 e §2: para onde cada camada pode olhar. */
class LayerRulesTest {

    private val frameworkPrefixes = listOf(
        "android.",
        "androidx.",
        "io.ktor.",
        "org.koin.",
        "org.jetbrains.compose.",
        "kotlinx.serialization.",
        "okio.",
    )

    @Test
    fun domain_nao_importa_framework() {
        val violations = ProjectSources.production
            .filter { it.layer == "domain" }
            .flatMap { file ->
                file.imports
                    .filter { import -> frameworkPrefixes.any { import.name.startsWith(it) } }
                    .map { "${file.relativePath}: import ${it.name}" }
            }

        assertNoViolations("domain não conhece Android, Compose, Ktor, Koin nem serialização", violations)
    }

    @Test
    fun data_nao_importa_presentation() {
        val forbidden = listOf(
            """^androidx\.compose\.""",
            """^org\.jetbrains\.compose\.""",
            """^androidx\.lifecycle\.""",
            """^$ROOT_PACKAGE\..*\.presentation\.""",
        ).joinToString("|").toRegex()
        val violations = ProjectSources.production
            .filter { it.layer == "data" }
            .flatMap { file ->
                file.imports
                    .filter { forbidden.containsMatchIn(it.name) }
                    .map { "${file.relativePath}: import ${it.name}" }
            }

        assertNoViolations("data não conhece presentation nem Compose", violations)
    }

    @Test
    fun presentation_nao_importa_data() {
        val forbidden =
            Regex("""^($ROOT_PACKAGE\.(core|feature\.\w+)\.data\.|io\.ktor\.|androidx\.datastore\.)""")
        val violations = ProjectSources.production
            .filter { it.feature != null && it.layer == "presentation" }
            .flatMap { file ->
                file.imports
                    .filter { forbidden.containsMatchIn(it.name) }
                    .map { "${file.relativePath}: import ${it.name}" }
            }

        assertNoViolations("presentation fala com o domain, nunca com data", violations)
    }

    @Test
    fun feature_nao_importa_outra_feature() {
        val featureImport = Regex("""^$ROOT_PACKAGE\.feature\.(\w+)\.""")
        val violations = ProjectSources.all
            .filter { it.feature != null }
            .flatMap { file ->
                file.imports
                    .filter { import ->
                        val importedFeature = featureImport.find(import.name)?.groupValues?.get(1)
                        importedFeature != null && importedFeature != file.feature
                    }
                    .map { "${file.relativePath}: import ${it.name}" }
            }

        assertNoViolations("feature não conhece outra feature", violations)
    }

    @Test
    fun core_nao_importa_feature() {
        val violations = ProjectSources.all
            .filter { it.module?.startsWith("core/") == true }
            .flatMap { file ->
                file.imports
                    .filter { it.name.startsWith("$ROOT_PACKAGE.feature.") }
                    .map { "${file.relativePath}: import ${it.name}" }
            }

        assertNoViolations("core não conhece feature", violations)
    }
}
