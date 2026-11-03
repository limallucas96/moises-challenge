import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * Testes de arquitetura com Konsist. O módulo lê o código-fonte de todos os outros,
 * mas não depende de nenhum. Rodar: ./gradlew :konsist:test
 */
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    // O Konsist lê arquivos .kt que não são input de compilação deste módulo. Declarados aqui,
    // o Gradle roda os testes de novo quando qualquer fonte do projeto muda.
    inputs.files(
        fileTree(rootDir) {
            include("**/*.kt")
            exclude("**/build/**", "**/.gradle/**", "**/.kotlin/**")
        },
    ).withPropertyName("projectSources").withPathSensitivity(PathSensitivity.RELATIVE)
}
