package cmp.skeleton.app.konsist

import com.lemonappdev.konsist.api.provider.KoAnnotationProvider
import kotlin.test.Test

/** ARCHITECTURE.md §4: toda NavKey é serializável e está registrada. */
class NavigationRulesTest {

    private class NavKeyDeclaration(
        val name: String,
        val isSerializable: Boolean,
        val file: String,
        val module: String?,
    )

    private val navKeys: List<NavKeyDeclaration> by lazy {
        ProjectSources.production.flatMap { file ->
            val classes = file.classes()
                .filter { it.hasParentWithName("NavKey") }
                .map { NavKeyDeclaration(it.name, it.hasSerializable(), file.relativePath, file.module) }
            val objects = file.objects()
                .filter { it.hasParentWithName("NavKey") }
                .map { NavKeyDeclaration(it.name, it.hasSerializable(), file.relativePath, file.module) }
            classes + objects
        }
    }

    @Test
    fun navkey_e_serializable_e_termina_em_Key() {
        val violations = navKeys.flatMap { key ->
            buildList {
                if (!key.isSerializable) add("${key.file}: ${key.name} sem @Serializable")
                if (!key.name.endsWith("Key")) add("${key.file}: ${key.name} não termina em Key")
            }
        }

        assertNoViolations("NavKey é @Serializable e se chama <Nome>Key", violations)
    }

    @Test
    fun navkey_esta_na_lista_da_feature() {
        val textByModule = ProjectSources.production.groupBy { it.module }
            .mapValues { (_, files) -> files.joinToString("\n") { it.text } }

        val violations = navKeys
            .filterNot { key ->
                Regex("""navKey\(\s*${key.name}\b""").containsMatchIn(textByModule[key.module].orEmpty())
            }
            .map { "${it.file}: ${it.name} não aparece em navKey(...) na lista <feature>NavKeys" }

        assertNoViolations("toda NavKey entra na lista <feature>NavKeys, senão a pilha não é restaurada", violations)
    }

    private fun KoAnnotationProvider.hasSerializable(): Boolean = annotations.any { it.name == "Serializable" }
}
