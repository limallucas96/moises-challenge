package com.lucaslima.musicsearch.konsist

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Se o filtro de arquivos quebrar, todas as outras regras passariam sem olhar nada.
 * Este teste garante que o escopo enxerga o projeto.
 */
class ProjectSourcesTest {

    @Test
    fun escopo_enxerga_producao_e_testes_das_camadas() {
        val modules = ProjectSources.all.mapNotNull { it.module }.toSet()

        val expected = listOf(
            "core/domain",
            "core/data",
            "core/playback",
            "feature/library/domain",
            "feature/library/data",
            "feature/library/presentation",
            "shared",
        )
        expected.forEach { module -> assertTrue(module in modules, "módulo $module não encontrado em $modules") }

        val productionCount = ProjectSources.production.size
        assertTrue(productionCount > 15, "poucos arquivos de produção: $productionCount")
        assertTrue(ProjectSources.tests.isNotEmpty(), "nenhum arquivo de teste encontrado")
        assertTrue(
            ProjectSources.tests.any { it.sourceSet == "androidDeviceTest" },
            "nenhum teste de androidDeviceTest encontrado",
        )
    }
}
