import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

// Compila com o JDK que roda o Gradle (o daemon está em JDK 21, ver
// gradle/gradle-daemon-jvm.properties) e gera bytecode 17. Sem jvmToolchain():
// ele exigiria um JDK 17 instalado na máquina.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// implementation, não compileOnly: os convention plugins são scripts pré-compilados com bloco
// plugins { id(...) }. Para gerar os accessors tipados (kotlin { }, android { }), o Gradle aplica
// esses plugins num projeto sintético durante o build do build-logic, então eles precisam estar
// no classpath de runtime. As versões vêm do mesmo catálogo que o build raiz usa.
dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
    implementation(libs.android.gradle.plugin)
    implementation(libs.compose.gradle.plugin)
}
