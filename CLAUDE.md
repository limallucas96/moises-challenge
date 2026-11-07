# CLAUDE.md

musicsearch: esqueleto base para projetos Kotlin Multiplatform + Compose Multiplatform (Android e iOS). Este
arquivo diz **como trabalhar** no projeto. As regras de código estão nos documentos abaixo e, sempre
que dá, em checagens automáticas. Não repita regra aqui: aponte para onde ela mora.

## Onde está cada coisa

- `ARCHITECTURE.md`: módulos, camadas, MVI, erros, DI, navegação, build, testes e o checklist de
  feature nova (§9). Leia antes de criar ou mover código.
- `docs/decisions/`: ADRs, o porquê de cada decisão. `index.md` lista todas com o status.
- `.claude/skills/`: skills da PL Coding, a convenção de referência. Quando skill e `ARCHITECTURE.md`
  divergem, vale o `ARCHITECTURE.md`.
- `konsist/`, `build-logic/convention/src/main/kotlin/app.module.rules.gradle.kts` e `.editorconfig`:
  as regras que a máquina verifica.

## Antes de dizer que terminou

Rode e confira que passou:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck :konsist:test testAndroidHostTest :androidApp:assembleDebug
```

- Mexeu em código iOS ou `commonMain`? Rode também `./gradlew iosSimulatorArm64Test` (precisa de
  Xcode). Se não der para rodar, diga que não rodou; o CI roda no PR.
- Mexeu em tela ou em `androidDeviceTest`? Rode também `./gradlew connectedAndroidDeviceTest`
  com um emulador ligado. Sem emulador, diga que não rodou; o CI roda no PR.
- Nunca diga que um build ou teste passou sem ter visto o resultado.

## Processo

- **ADR:** pergunte antes de escrever uma. ADR nova entra como `proposed` no `index.md`. ADR
  `accepted` não é editada: decisão que muda vira ADR nova que a substitui.
- **Regra nova:** entra na camada mais barata que a verifica (Gradle → Konsist → ktlint → texto),
  junto com a atualização do `ARCHITECTURE.md` no mesmo commit.
- **Commits:** skill `git-commit` (Conventional Commits em inglês, um assunto por commit). Trabalho em
  branch e PR; não faça push sem pedido.
- **Dependências:** versão só no `gradle/libs.versions.toml`. Confira que a versão existe no Maven
  antes de usar e que a biblioteca é KMP se for para `commonMain`.
- **Idioma:** documentação, KDoc e comentários em português; nomes no código em inglês; testes em
  snake_case descrevendo o comportamento.
- **Escopo:** faça o que foi pedido. Mudança de arquitetura, lib nova ou exceção a uma regra é
  pergunta para o dono do projeto antes de virar código.
