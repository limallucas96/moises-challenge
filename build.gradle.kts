plugins {
    // Declarados aqui (apply false) só para carregar o classpath uma vez;
    // quem aplica de verdade são os convention plugins de build-logic.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.spotless)
}

// Formatação e regras de Compose para o repositório inteiro . As regras ficam no
// .editorconfig da raiz. Conferir: ./gradlew spotlessCheck. Corrigir: ./gradlew spotlessApply.
// .claude/ fica de fora: são skills de terceiros, copiadas como vieram.
spotless {
    val editorConfig = rootProject.file(".editorconfig")
    val ktlintVersion = libs.versions.ktlint.get()

    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", "**/.gradle/**", "**/.kotlin/**", ".claude/**")
        ktlint(ktlintVersion)
            .setEditorConfigPath(editorConfig)
            .customRuleSets(listOf("io.nlopez.compose.rules:ktlint:${libs.versions.compose.rules.get()}"))
        // Únicos CompositionLocal permitidos: o tema estendido. A allowlist do
        // .editorconfig e o @Suppress não chegam à regra quando ela roda pelo Spotless.
        listOf("AppColors.kt", "AppTypography.kt").forEach { file ->
            suppressLintsFor {
                step = "ktlint"
                shortCode = "compose:compositionlocal-allowlist"
                path = "core/designsystem/src/commonMain/kotlin/com/lucaslima/musicsearch/core/designsystem/theme/$file"
            }
        }
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", "**/.gradle/**", ".claude/**")
        ktlint(ktlintVersion).setEditorConfigPath(editorConfig)
    }
}
