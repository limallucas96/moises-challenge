package cmp.skeleton.app.konsist

import kotlin.test.Test

/** ARCHITECTURE.md §9: fakes em vez de mocks, tempo controlado em vez de espera real. */
class TestRulesTest {

    @Test
    fun testes_nao_usam_biblioteca_de_mock() {
        val violations = ProjectSources.tests.flatMap { file ->
            file.imports
                .filter { it.name.startsWith("io.mockk.") || it.name.startsWith("org.mockito.") }
                .map { "${file.relativePath}: import ${it.name}" }
        }

        assertNoViolations("teste usa fake escrito à mão (Fake<Nome>), não mock", violations)
    }

    @Test
    fun testes_nao_usam_Thread_sleep() {
        val violations = ProjectSources.tests
            .filter { "Thread.sleep(" in it.text }
            .map { it.relativePath }

        assertNoViolations("teste não espera tempo real: use runTest/advanceTimeBy", violations)
    }

    // --- Testes instrumentados ---

    @Test
    fun teste_de_ui_do_compose_so_roda_em_aparelho() {
        val violations = ProjectSources.tests
            .filter { it.sourceSet != DEVICE_TEST }
            .flatMap { file ->
                file.imports
                    .filter { it.name.startsWith("androidx.compose.ui.test.") }
                    .map { "${file.relativePath}: import ${it.name}" }
            }

        assertNoViolations("teste de UI fica em androidDeviceTest, não na JVM", violations)
    }

    @Test
    fun testes_nao_usam_robolectric_nem_a_regra_do_compose_sem_activity() {
        val violations = ProjectSources.tests.flatMap { file ->
            file.imports
                .filter { it.name.startsWith("org.robolectric.") || it.name.endsWith(".createComposeRule") }
                .map { "${file.relativePath}: import ${it.name}" }
        }

        assertNoViolations("teste de UI usa createAndroidComposeRule<ComponentActivity>() em aparelho", violations)
    }

    @Test
    fun classe_de_teste_em_aparelho_termina_em_DeviceTest() {
        // O runner só executa classes com DeviceTest no nome (app.device.test): outra passaria sem rodar.
        val violations = ProjectSources.tests
            .filter { it.sourceSet == DEVICE_TEST && "@Test" in it.text && !it.fileName.endsWith("DeviceTest") }
            .map { it.relativePath }

        assertNoViolations("arquivo com @Test em androidDeviceTest se chama <Nome>DeviceTest", violations)
    }

    @Test
    fun classe_DeviceTest_so_existe_em_androidDeviceTest() {
        val violations = ProjectSources.tests
            .filter { it.sourceSet != DEVICE_TEST && it.fileName.endsWith("DeviceTest") }
            .map { it.relativePath }

        assertNoViolations("o sufixo DeviceTest é dos testes de androidDeviceTest", violations)
    }

    private companion object {
        const val DEVICE_TEST = "androidDeviceTest"
    }
}
