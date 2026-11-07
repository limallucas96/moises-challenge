/*
 * Regras de dependência entre módulos (ARCHITECTURE.md §2). Aplicado pelo
 * app.kmp.library, então vale para todo módulo core e feature. A configuração do Gradle falha
 * assim que um build.gradle.kts (ou convention plugin) declara uma dependência proibida.
 */
configurations.configureEach {
    val configurationName = name
    dependencies.withType<ProjectDependency>().configureEach {
        val target = this.path
        moduleRuleViolation(from = project.path, to = target)?.let { reason ->
            throw GradleException(
                "${project.path} -> $target ($configurationName): $reason. " +
                    "Ver ARCHITECTURE.md §2.",
            )
        }
    }
}

fun moduleRuleViolation(
    from: String,
    to: String,
): String? {
    // O KMP liga o teste de um módulo ao próprio main por uma dependência de projeto nele mesmo.
    if (from == to) return null

    val source = from.removePrefix(":").split(":")
    val target = to.removePrefix(":").split(":")

    if (target.first() == "shared" || target.first() == "androidApp") {
        return "só o :androidApp depende do :shared"
    }
    if (source.first() == "core" && target.first() == "feature") {
        return "core não depende de feature"
    }
    if (source.first() != "feature" || source.size < 3) return null

    val feature = source[1]
    val layer = source[2]
    return when (target.first()) {
        "feature" -> when {
            target.getOrNull(1) != feature -> "feature não depende de outra feature "
            target.getOrNull(2) == "domain" && layer != "domain" -> null
            else -> "dentro da feature, só data e presentation dependem do domain"
        }

        "core" -> {
            val allowed = when (layer) {
                "domain" -> setOf("domain", "testing")

                // data adapta a porta do player (:core:playback) para o modelo da feature.
                "data" -> setOf("domain", "data", "playback", "testing")

                "presentation" -> setOf("domain", "presentation", "designsystem", "testing")

                else -> return null
            }
            if (target.getOrNull(1) in allowed) null else "$layer não depende de :core:${target.getOrNull(1)}"
        }

        else -> null
    }
}
