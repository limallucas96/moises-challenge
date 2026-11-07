package com.lucaslima.musicsearch.konsist

import kotlin.test.Test

/** ARCHITECTURE.md §1.7 e §6: o nome diz o que a coisa é. */
class NamingRulesTest {

    @Test
    fun nenhuma_classe_ou_interface_termina_em_Impl() {
        val violations = ProjectSources.all.flatMap { file ->
            (file.classes().map { it.name } + file.interfaces().map { it.name })
                .filter { it.endsWith("Impl") }
                .map { "${file.relativePath}: $it" }
        }

        assertNoViolations(
            "sem sufixo Impl: o nome diz qual implementação é (RoomXDataSource, KtorXDataSource)",
            violations,
        )
    }

    @Test
    fun modulo_koin_termina_em_Module() {
        val koinModule = Regex("""\bval\s+(\w+)\s*(?::\s*Module\s*)?=\s*module\s*\{""")
        val violations = ProjectSources.production.flatMap { file ->
            koinModule.findAll(file.text)
                .map { it.groupValues[1] }
                .filterNot { it.endsWith("Module") }
                .map { "${file.relativePath}: $it" }
                .toList()
        }

        assertNoViolations("módulo Koin se chama <nome><Camada>Module", violations)
    }
}
