# cmpskeleton

Esqueleto base para projetos Kotlin Multiplatform + Compose Multiplatform (Android e iOS). Traz a
arquitetura, as regras verificadas pela máquina (Gradle, Konsist, ktlint), o CI e as skills do Claude,
com duas features de exemplo (`alpha` e `beta`) que exercitam módulos, DI e navegação.

- Arquitetura: [`ARCHITECTURE.md`](ARCHITECTURE.md)
- Decisões (ADRs): [`docs/decisions/`](docs/decisions/index.md)
- Como trabalhar no projeto (pessoas e Claude): [`CLAUDE.md`](CLAUDE.md)

## Começando um projeto novo

1. Copie a pasta e rode `git init`.
2. Troque o pacote `cmp.skeleton.app` pelo do projeto: nas pastas `src/*/kotlin/cmp/skeleton/app`, no
   código, no `build-logic` (namespace derivado do path), nos `packageOfResClass`, no `ROOT_PACKAGE` do
   `:konsist`, no `applicationId` e no `bundleId` do `:shared`.
3. Troque `rootProject.name` (`settings.gradle.kts`, sem ponto), o `app_name` do Android e o
   `PRODUCT_NAME` / `PRODUCT_BUNDLE_IDENTIFIER` de `iosApp/Configuration/Config.xcconfig`.
4. Atualize o "Estado do código" e as lacunas (§10) do `ARCHITECTURE.md` e a primeira linha do
   `CLAUDE.md`.
5. Crie as features do projeto pelo checklist do `ARCHITECTURE.md` (§9) e apague `alpha` e `beta`
   quando não servirem mais de exemplo (lembre do `ProjectSourcesTest` do `:konsist`).

## Rodando

- Android: `./gradlew :androidApp:assembleDebug` (ou rodar o `androidApp` no Android Studio).
- iOS: abrir `iosApp/iosApp.xcodeproj` no Xcode e rodar. Preencha `TEAM_ID` em
  `iosApp/Configuration/Config.xcconfig` para rodar em aparelho.

O Gradle roda no JDK 21 (`gradle/gradle-daemon-jvm.properties`) e baixa o JDK sozinho se faltar.
O Android SDK é lido do `local.properties` (`sdk.dir=...`), que não vai para o git.

## Testes

- Android (JVM): `./gradlew testAndroidHostTest`
- iOS (simulador): `./gradlew iosSimulatorArm64Test`
- Em aparelho (UI): `./gradlew connectedAndroidDeviceTest` (com emulador ligado)
- Arquitetura (Konsist): `./gradlew :konsist:test`

## Formatação e lint

- Conferir: `./gradlew spotlessCheck`
- Corrigir o que for automático: `./gradlew spotlessApply`
