package cmp.skeleton.app.konsist

import kotlin.test.Test

/** ARCHITECTURE.md §3 (Presentation: MVI) e skill android-presentation-mvi. */
class PresentationRulesTest {

    @Test
    fun viewmodel_termina_em_ViewModel_fica_em_presentation_e_expoe_state() {
        val violations = ProjectSources.production.flatMap { file ->
            file.classes()
                .filter { it.hasParentWithName("ViewModel") }
                .flatMap { viewModel ->
                    val where = "${file.relativePath}: ${viewModel.name}"
                    buildList {
                        if (!viewModel.name.endsWith("ViewModel")) add("$where não termina em ViewModel")
                        if (file.feature == null || file.layer != "presentation") {
                            add("$where fora de :feature:*:presentation")
                        }
                        if (viewModel.properties().none { it.name == "state" }) add("$where não expõe `state`")
                    }
                }
        }

        assertNoViolations("ViewModel mora no presentation da feature e expõe `state`", violations)
    }

    @Test
    fun arquivo_Screen_tem_Root_e_Screen() {
        val violations = ProjectSources.production
            .filter { it.layer == "presentation" && it.fileName.endsWith("Screen") }
            .flatMap { file ->
                val base = file.fileName.removeSuffix("Screen")
                listOf("${base}Root", "${base}Screen")
                    .filterNot { Regex("""\bfun\s+$it\s*\(""").containsMatchIn(file.text) }
                    .map { "${file.relativePath}: falta `$it`" }
            }

        assertNoViolations("tela = <Nome>Root (ViewModel) + <Nome>Screen (estado) no mesmo arquivo", violations)
    }
}
