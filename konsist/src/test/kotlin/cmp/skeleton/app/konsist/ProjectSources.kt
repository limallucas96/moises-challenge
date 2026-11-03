package cmp.skeleton.app.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import java.io.File
import kotlin.test.assertTrue

/** Pacote raiz do app; as regras de camada comparam imports contra ele. */
internal const val ROOT_PACKAGE = "cmp.skeleton.app"

/** Raiz do repositório: o primeiro diretório acima com settings.gradle.kts e gradlew. */
private val rootPath: String =
    generateSequence(File("").absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").exists() && File(it, "gradlew").exists() }
        .path
        .replace('\\', '/')

/** `feature/auth/data/src/commonMain/kotlin/...` */
internal val KoFileDeclaration.relativePath: String
    get() = path.replace('\\', '/').removePrefix("$rootPath/")

internal val KoFileDeclaration.fileName: String
    get() = relativePath.substringAfterLast('/').removeSuffix(".kt")

/** `feature/auth/data`, `core/domain`, `shared`. */
internal val KoFileDeclaration.module: String?
    get() = relativePath.substringBefore("/src/", missingDelimiterValue = "").ifEmpty { null }

/** `commonMain`, `androidMain`, `commonTest`... */
internal val KoFileDeclaration.sourceSet: String?
    get() = Regex("/src/([^/]+)/").find(relativePath)?.groupValues?.get(1)

internal val KoFileDeclaration.isTest: Boolean
    get() = sourceSet?.endsWith("Test") == true

/** Nome da feature (`auth`) ou null fora de `feature/`. */
internal val KoFileDeclaration.feature: String?
    get() = module?.split('/')?.takeIf { it.first() == "feature" }?.getOrNull(1)

/** `domain`, `data` ou `presentation` para feature; o nome do core para `core/x`. */
internal val KoFileDeclaration.layer: String?
    get() {
        val parts = module?.split('/') ?: return null
        return when (parts.first()) {
            "feature" -> parts.getOrNull(2)
            "core" -> parts.getOrNull(1)
            else -> null
        }
    }

internal object ProjectSources {
    /** Todo .kt dos módulos do app. Fica de fora: build-logic, o próprio :konsist e build/. */
    val all: List<KoFileDeclaration> by lazy {
        Konsist.scopeFromProject().files.filter { file ->
            val relative = file.relativePath
            relative.endsWith(".kt") &&
                "/build/" !in relative &&
                file.module != null &&
                !relative.startsWith("build-logic/") &&
                !relative.startsWith("konsist/")
        }
    }

    val production: List<KoFileDeclaration> by lazy { all.filterNot { it.isTest } }

    val tests: List<KoFileDeclaration> by lazy { all.filter { it.isTest } }
}

internal fun assertNoViolations(
    rule: String,
    violations: List<String>,
) {
    assertTrue(
        violations.isEmpty(),
        "$rule:\n" + violations.joinToString("\n") { "  - $it" },
    )
}
