# Arquitetura — com.lucaslima.musicsearch (Compose Multiplatform)

> Este documento descreve o *como*. O *porquê* de cada escolha nova vai para
> [`docs/decisions/`](docs/decisions/index.md).
>
> **Estado do código:** app de busca de músicas (API do iTunes) construído sobre o esqueleto
> cmpskeleton. Uma feature, `library` (`domain`, `data`, `presentation`), com as telas Songs,
> Player e Album, mais o `:core:playback` (porta do player). As seções abaixo ainda usam as
> features de exemplo do esqueleto (`alpha`, `beta`) para ilustrar as convenções.

Alvos: **Android** e **iOS**, UI 100% compartilhada em Compose Multiplatform.

Convenção de referência: as skills da [PL Coding](https://github.com/PL-Coding-GmbH/Skills), em
`.claude/skills/`. Quando elas divergem deste documento, vale este documento.

---

## 1. Princípios

1. **A feature é a unidade de mudança.** Quem abre o projeto vê *o que o app faz* (`alpha`, `beta`).
   Cada feature tem até três módulos: `domain`, `data` e `presentation`.
2. **A dependência aponta para o domain.** `presentation → domain ← data`. O domain não conhece
   Compose, Ktor nem Android, e o compilador garante isso: essas bibliotecas não estão no
   classpath dele. Exceção única: `androidx.paging` (`paging-common`, Kotlin puro e KMP), porque o
   contrato do repositório devolve `PagingData`. O `:konsist` libera só esse prefixo.
3. **Feature não conhece feature.** Modelo compartilhado de verdade sobe para `:core:domain`.
   Navegação entre features é callback, montado no `:shared`.
4. **`internal` por camada.** No domain, as interfaces e os modelos são públicos (são o contrato).
   No data, as implementações são `internal`. No presentation, ViewModel e telas são `internal`.
   Só saem de cada módulo o módulo Koin e, no presentation, a função do grafo de navegação, a
   `NavKey` de entrada e a lista de keys.
5. **Falha esperada é valor de retorno.** `Result<D, E : Error>`, com o erro declarado pela camada
   que o produz. Exceção só é capturada na camada dona dela.
6. **O nome diz o que a classe é.** `InMemoryGreetingRepository`, `RoomXDataSource`,
   `KtorXDataSource`. Sem sufixo `Impl` (o Konsist reprova).
7. **Só biblioteca KMP no `commonMain`.** Biblioteca de uma plataforma só (OkHttp, Darwin,
   Credential Manager) entra apenas em `androidMain`/`iosMain` ou no `:androidApp`.

---

## 2. Mapa de módulos

```
                    ┌─────────────┐     ┌──────────┐
                    │ :androidApp │     │  iosApp  │          entry points
                    └──────┬──────┘     └────┬─────┘
                           └────────┬────────┘
                            ┌───────▼───────┐
                            │    :shared    │  composition root: App, NavDisplay,
                            │               │  Koin, ligação entre features
                            └───────┬───────┘
                      ┌─────────────┴─────────────┐
              ┌───────▼────────┐          ┌───────▼────────┐
              │ :feature:alpha │          │ :feature:beta  │
              │ presentation   │          │ presentation   │
              │     │          │          └────────────────┘
              │     ▼          │
              │  domain ◄─ data│
              └────────────────┘

   :core:domain · :core:data · :core:presentation · :core:designsystem · :core:testing
```

### Regras de dependência

| Módulo | Pode depender de | Nunca |
|---|---|---|
| `:feature:x:domain` | `:core:domain` | qualquer outro módulo |
| `:feature:x:data` | `:feature:x:domain`, `:core:domain`, `:core:data`, `:core:playback` | presentation, outra feature |
| `:feature:x:presentation` | `:feature:x:domain`, `:core:domain`, `:core:presentation`, `:core:designsystem` | data, outra feature |
| `:core:*` | outro `:core:*` (grafo acíclico) | qualquer `:feature:*` |
| `:shared` | tudo | — |
| `:androidApp` / `iosApp` | `:shared` | qualquer outro |

`:core:domain` está disponível para todo módulo. Esta tabela é verificada no build: o plugin
`app.module.rules` (aplicado pelo `app.kmp.library`) falha a configuração do Gradle quando um módulo
`core` ou `feature` declara uma dependência fora dela. `:core:testing` é permitido em qualquer
camada, só para testes. Os imports também são verificados, pelo `:konsist`.

### O que mora em cada `:core`

| Módulo | Responsabilidade | O que **não** entra |
|---|---|---|
| `core:domain` | `Result`, `EmptyResult`, `Error`, `DataError`, `DataErrorException` (leva o `DataError` pelo `LoadState.Error` do Paging) e os helpers `map`/`mapError`/`onSuccess`/`onFailure` | qualquer coisa com I/O ou UI |
| `core:data` | `HttpClientFactory` (engine injetada; aceita JSON também como `text/javascript`), `safeCall` / `responseToResult`, `HttpClient.safeGet/safePost`, porta `AuthTokenProvider`, o `DataStore<Preferences>` do app | DTO ou endpoint de feature |
| `core:presentation` | `UiText` (+ `asStringSuspend()`), `ObserveAsEvents`, `DataError.toUiText()` e as strings de erro, `NavKeyRegistration` | tela concreta, ViewModel de feature |
| `core:designsystem` | tema (`AppTheme`, que segue o tema do sistema), tema estendido (`AppTheme.colors`, `AppTheme.typography`), `Spacing`, `Radius` | estado de tela, lógica; `CompositionLocal` além de `LocalAppColors`/`LocalAppTypography` (o ktlint reprova; esses dois estão no `suppressLintsFor` do `build.gradle.kts` raiz) |
| `core:playback` | porta `AudioPlayer` + `PlaybackState` no `commonMain`; Media3 no `androidMain` (`PlaybackService` dono do ExoPlayer e da MediaSession, o app fala com ele por MediaController), AVPlayer no `iosMain` (fila, repetir e Now Playing próprios). `platformPlaybackModule` registra a implementação no Koin. Não conhece modelo de feature: recebe `PlayableItem` | regra de negócio; o adapter para o modelo da feature fica no data da feature |
| `core:testing` | `runViewModelTest` e o bundle de testes como `api`; no Android, `text()` e `pluralText()` para o teste de UI ler Compose Resources | código de produção; fakes de interfaces de feature (ficam no `commonTest` da feature) |

---

## 3. Anatomia de uma feature

A `alpha` é o exemplo completo:

```
feature/alpha/
├── domain/                              :feature:alpha:domain        app.kmp.library
│   └── .../alpha/domain/
│       ├── Greeting.kt                  public   modelo puro
│       └── GreetingRepository.kt        public   o domain dita o contrato
│
├── data/                                :feature:alpha:data          app.kmp.data
│   └── .../alpha/data/
│       ├── di/AlphaDataModule.kt        public   Koin da camada
│       └── InMemoryGreetingRepository.kt internal
│
└── presentation/                        :feature:alpha:presentation  app.cmp.feature
    └── .../alpha/presentation/
        ├── di/AlphaPresentationModule.kt        public
        ├── navigation/AlphaNavigation.kt        public   AlphaKey + alphaEntries(...) + alphaNavKeys
        ├── AlphaContract.kt                     internal State + Action (+ Event, se precisar)
        ├── AlphaViewModel.kt                    internal
        └── AlphaScreen.kt                       internal Root + Screen no mesmo arquivo
```

Quando a feature tem fonte de dados real, cada conceito ganha três modelos, de propósito: `Entity`
(persistência), `Dto` (contrato da API) e o modelo do domain. Fundir economiza código hoje e cobra
um refactor no dia em que a API mudar um campo. Quando a tela precisa de formatação, entra um
quarto, o `*Ui`, no presentation.

A `beta` mostra o caso mínimo: só `presentation`, sem dados, com um argumento que chega pela
`NavKey` (`BetaKey(message)`) e vira parâmetro do ViewModel via `koinViewModel { parametersOf(...) }`.

### Data layer: DataSource × Repository

- **DataSource** acessa *uma* fonte (API, disco). **Todo data source tem interface**, para o
  repository ser testável com fake. A interface mora no domain quando o presentation ou um use case
  a usam; quando só o repository usa e ela fala em DTO, fica `internal` no data.
- **Repository** só existe quando coordena mais de uma fonte, ou quando o domain precisa do contrato.
- **Use case** só existe quando há regra. Um use case que só repassa a chamada do repositório não é
  criado: o ViewModel chama o repositório direto.

### Presentation: MVI

| Peça | Convenção | Regra |
|---|---|---|
| `AlphaState` | `data class`, sempre completo | atualizado com `_state.update { it.copy(...) }` |
| `AlphaAction` | `sealed interface`, `On<Coisa>Click` | única entrada do ViewModel: `onAction(action)` |
| `AlphaEvent` | `sealed interface`, **opcional** | só para efeito único que só o ViewModel conhece |
| `AlphaRoot` | segura o ViewModel via `koinViewModel()` | consome `events` com `ObserveAsEvents` |
| `AlphaScreen` | recebe `state`, `onAction` e callbacks | sem ViewModel: tem `@Preview` e teste isolado |

**Quando criar `Event`:** navegar depois de uma chamada assíncrona que deu certo (login concluído),
ou mostrar um snackbar por um erro que voltou do repositório. **Quando não criar:** navegação por um
clique simples (o `Screen` chama o callback direto, como o "Open Beta" da Alpha), ou diálogo cuja
visibilidade cabe no `State`. Se a tela não tem nenhum desses casos, ela não tem `Event`, `Channel`
nem `ObserveAsEvents`.

**Process death:** formulário com entrada crítica restaura os campos essenciais via
`SavedStateHandle`. Só o que importa, não o `State` inteiro.

**Texto:** o que vem de string resource ou pode ser traduzido é `UiText`. Valor sempre dinâmico
(nome do usuário, data formatada) é `String`.

---

## 4. Comunicação entre features

1. **Navegação por callback (Navigation 3).** O `:shared` é dono da pilha (`rememberNavBackStack`).
   Cada feature expõe as suas telas como extensão de `EntryProviderScope<NavKey>` e recebe lambdas
   para tudo que sai dela. Este é o código real do esqueleto:

   ```kotlin
   // :feature:beta:presentation
   @Serializable data class BetaKey(val message: String) : NavKey      // entrada: pública

   fun EntryProviderScope<NavKey>.betaEntries(onBackClick: () -> Unit) {
       entry<BetaKey> { key -> BetaRoot(message = key.message, onBackClick = onBackClick) }
   }

   val betaNavKeys: List<NavKeyRegistration<*>> = listOf(               // TODAS as keys da feature
       navKey(BetaKey(message = "sample")),
   )

   // :shared
   internal val allNavKeys = alphaNavKeys + betaNavKeys
   NavDisplay(
       backStack = backStack,
       entryProvider = entryProvider {
           alphaEntries(onOpenBetaClick = { message -> backStack.add(BetaKey(message)) })
           betaEntries(onBackClick = { backStack.removeLastOrNull() })
       },
       /* ... */
   )
   ```

   Só a key de entrada é pública, porque o `:shared` precisa dela; keys de telas internas são
   `internal`. Uma feature nunca importa key de outra. **Toda `NavKey` precisa estar na lista
   `<feature>NavKeys` da feature, e toda lista no `allNavKeys`:** uma key esquecida só quebra quando
   o app restaura o estado (morte de processo no Android, sempre no iOS). O `NavKeyRegistrationTest`
   do `:shared` faz o round-trip de cada key do `allNavKeys`, e o `:konsist` confere que toda
   `NavKey` é `@Serializable`, termina em `Key` e aparece em `navKey(...)`.

   O callback que empilha tela confere o topo da pilha (`if (backStack.lastOrNull() is AlphaKey)`):
   um segundo toque durante a transição não empilha a tela duas vezes.

2. **Porta + adapter.** Quem precisa declara a interface; quem sabe responder implementa.
   - `:core:data` declara `AuthTokenProvider`; a feature de auth (quando existir) implementa e
     registra no Koin. O Ktor ganha o Bearer token sem a camada de rede saber o que é login.
   - Quando a porta liga duas features, o adapter mora no `:shared`, o único módulo que conhece as
     duas (módulo Koin `featureBridgesModule`).

3. **Modelo compartilhado.** Se duas features passarem a compartilhar domínio, o modelo sobe para
   `:core:domain`, ou vira uma feature própria cujo domain as outras não importam, consultada por
   porta. Nunca uma dependência entre features.

---

## 5. Erros

```kotlin
// :core:domain
interface Error
sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.lucaslima.musicsearch.core.domain.Error>(val error: E) : Result<Nothing, E>
}
typealias EmptyResult<E> = Result<Unit, E>

sealed interface DataError : Error {
    enum class Network : DataError { BAD_REQUEST, REQUEST_TIMEOUT, UNAUTHORIZED, FORBIDDEN,
        NOT_FOUND, CONFLICT, TOO_MANY_REQUESTS, NO_INTERNET, PAYLOAD_TOO_LARGE, SERVER_ERROR,
        SERVICE_UNAVAILABLE, SERIALIZATION, UNKNOWN }
    enum class Local : DataError { DISK_FULL, NOT_FOUND, UNKNOWN }
}
```

| Onde | Tipo de retorno |
|---|---|
| RemoteDataSource | `Result<T, DataError.Network>` |
| LocalDataSource | `Result<T, DataError.Local>` |
| Repository (várias fontes) | `Result<T, DataError>` |
| Regra de domínio | `EmptyResult<MeuErroDeValidacao>` |
| Erro de feature | `enum class XError : Error` — o repository traduz o `DataError` para ele |

| Exceção vinda de | Capturada em | Vira |
|---|---|---|
| Ktor / rede | data (`safeCall`) | status HTTP → `DataError.Network.*`; `IOException` → `NO_INTERNET`; qualquer outra → `UNKNOWN` |
| Disco | data (DataSource) | `DataError.Local.*` |
| Regra de negócio | domain | enum de erro da feature |

Cada erro exibível tem `.toUiText()`: os compartilhados em `:core:presentation`, os da feature no
presentation dela. `CancellationException` sempre é relançada.

---

## 6. Injeção de dependência (Koin)

- Um módulo Koin por camada que tenha algo a prover: `alphaDataModule`, `alphaPresentationModule`.
  Domain não tem módulo Koin; os use cases são registrados no módulo de presentation. O nome do
  módulo termina em `Module` (o Konsist confere).
- `singleOf(::InMemoryGreetingRepository) { bind<GreetingRepository>() }`,
  `viewModelOf(::AlphaViewModel)`. O lambda (`single { ... }`, `viewModel { params -> ... }`) só
  entra quando o construtor não basta (factory, qualifier, parâmetro vindo da `NavKey`).
- Tudo é montado no `:shared` (`appModules()`). Nenhum módulo de feature chama `startKoin`.
- Dependências de plataforma (Context, caminho de arquivo, SDK de login) entram como módulos
  `expect/actual`, como o `platformCoreDataModule` do `:core:data`.
- Dispatchers só são injetados em classe que troca de dispatcher **e** tem teste unitário. Nos
  testes de ViewModel, `Dispatchers.setMain(UnconfinedTestDispatcher())` (`runViewModelTest`).

**Preferências e flags** usam o `DataStore<Preferences>` de `:core:data` (API `suspend`/`Flow`, fora
da main thread). Dado sensível (token de sessão) não vai no DataStore, que não criptografa: vai para
Keychain (iOS) e armazenamento criptografado (Android).

---

## 7. Build

### Convention plugins (`build-logic/`)

```
app.kmp.library   targets Android + iOS, namespace pelo path, SDK, coroutines, testes
                  (kotlin-test, coroutines-test, Turbine, AssertK), flags de compilador
app.module.rules  aplicado pelo kmp.library: falha o build se a dependência entre módulos
                  fugir da tabela de §2
app.kmp.data      = kmp.library + serialization + Ktor (bundle) + Koin
app.cmp.library   = kmp.library + Compose + ui-tooling em androidRuntimeClasspath
app.cmp.feature   = cmp.library + Koin compose + Navigation 3 + ui-backhandler + lifecycle-viewmodel-navigation3
                    + :core:presentation + :core:designsystem + app.device.test
app.device.test   liga o androidDeviceTest (testes em aparelho): herda o commonTest, usa o
                  AndroidJUnitRunner e só roda classes terminadas em DeviceTest
```

| Módulo | Plugin |
|---|---|
| `:feature:x:domain`, `:core:domain`, `:core:testing` | `app.kmp.library` |
| `:feature:x:data`, `:core:data` | `app.kmp.data` |
| `:feature:x:presentation` | `app.cmp.feature` |
| `:core:presentation`, `:core:designsystem` | `app.cmp.library` |
| `:shared` | `app.cmp.library` + framework iOS |

### Regras

- **Versões só no `gradle/libs.versions.toml`**, com nome em `[versions]`. Aliases em kebab-case
  derivados do artifactId (`compose-ui-tooling-preview`, `kotlin-multiplatform`); convention plugins
  com alias `convention-*` (`libs.plugins.convention.cmp.feature`) e ID `app.*`. Artefatos usados
  sempre juntos viram bundle (`bundles.ktor`, `bundles.koin.compose`, `bundles.testing`).
- **Project accessors tipados** (`TYPESAFE_PROJECT_ACCESSORS`): `projects.core.domain`, nunca
  `project(":core:domain")`. Por isso o `rootProject.name` não pode ter ponto (`musicsearch`).
- **Flags de compilador**, aplicadas pelo `app.kmp.library`: `-Xexpect-actual-classes`,
  `-opt-in=kotlin.time.ExperimentalTime`, `-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi`.
- **Previews:** módulo KMP não tem `debugImplementation`. O `ui-tooling` entra via
  `androidRuntimeClasspath` em todo módulo Compose (exige AGP 9+); sem isso, `@Preview` não renderiza.
- **JDK:** o Gradle roda no JDK 21 definido em `gradle/gradle-daemon-jvm.properties`. O `build-logic`
  gera bytecode 17 sem `jvmToolchain()`, para não exigir um segundo JDK instalado.
- **JVM 17** em todos os módulos (Kotlin `jvmTarget` e `compileOptions` do `:androidApp`).
- **minSdk 26**, compileSdk/targetSdk 36.
- **Namespace Android** derivado do path: `:feature:alpha:data` → `com.lucaslima.musicsearch.feature.alpha.data`.
- **Compose Resources:** cada módulo com `composeResources` declara `packageOfResClass` no próprio
  `build.gradle.kts` (`com.lucaslima.musicsearch.feature.alpha.presentation.resources`).
- **Segredos e URL base:** hoje o `API_BASE_URL` é uma constante em `:shared/di/AppModules.kt`, com
  valor de exemplo. O destino é BuildKonfig lendo do `local.properties`.
- **Formatação e lint:** ktlint 1.8 (estilo `android_studio`, linha de 120) com as
  [compose-rules](https://mrmans0n.github.io/compose-rules/), via Spotless na raiz. A configuração
  fica no `.editorconfig`. `./gradlew spotlessCheck` confere e `./gradlew spotlessApply` corrige o
  que for automático.

---

## 8. Testes

| Módulo | O que testa | Com o quê |
|---|---|---|
| `domain` | use cases e regras | puro, sem fake de infraestrutura |
| `data` | repository, data source, `safeCall` | fakes dos data sources; `MockEngine` do Ktor |
| `presentation` | ViewModel: estado, eventos, ação repetida, ação durante operação em andamento | `runViewModelTest`, Turbine, fakes |
| `presentation` (`androidDeviceTest`) | a tela em aparelho: o `Root` com o ViewModel de verdade e toque real | `createAndroidComposeRule<ComponentActivity>()`, os fakes do `commonTest` |
| `:shared` | o registro das `NavKey` (round-trip de cada key) | kotlin-test, AssertK |
| `:konsist` | regras de arquitetura lendo o código-fonte: camadas, imports entre features, sem `Impl`, ViewModel/Root/Screen, `NavKey` registrada, sem mock | Konsist |

- **Fake em vez de mock.** Os fakes ficam no `commonTest` da feature que os usa, com toggles de erro.
- **kotlin-test, não JUnit5.** JUnit5 não roda em `commonTest`; os testes rodam em Android (host) e iOS.
- **AssertK** para asserções, **Turbine** para `Flow`.
- **Teste em aparelho:** `./gradlew connectedAndroidDeviceTest`, com um emulador ligado. A classe
  termina em `DeviceTest`, senão o runner não a executa (o `:konsist` confere). O nó é procurado por
  texto, e o texto vem da mesma string da tela: `text(Res.string.x)`. Nada de Robolectric nem de
  `createComposeRule()` na JVM.
- **CI** (`.github/workflows/ci.yml`): em todo PR e push na `main`, um job Linux roda
  `spotlessCheck`, `:konsist:test`, `testAndroidHostTest` e `:androidApp:assembleDebug`; em PR, um job
  macOS roda `iosSimulatorArm64Test` e um job Linux com emulador roda `connectedAndroidDeviceTest`.
- **Regra nova de arquitetura vira teste no `:konsist`** quando dá para verificar por nome, pacote
  ou import. Só fica apenas no texto o que nenhuma ferramenta consegue checar.
- Tempo, id e dispatcher são injetados para que nenhum teste dependa de relógio real,
  `Thread.sleep` ou `startKoin` global.

---

## 9. Checklist: feature nova

1. Criar `feature/<nome>/{domain,data,presentation}` e incluir no `settings.gradle.kts`. O `data`
   só existe se a feature tiver fonte de dados (a `beta` não tem).
2. `domain` → `app.kmp.library`; `data` → `app.kmp.data`; `presentation` → `app.cmp.feature`.
3. Domain: modelos, interfaces (repository / data source), enum de erro, use case só se houver regra.
4. Data: DTO, mappers, data sources, repository se houver mais de uma fonte. Tudo `internal`.
5. Data: `<nome>DataModule` com `singleOf(...) { bind<...>() }`.
6. Presentation: para cada tela, `State`, `Action`, `Event` (se precisar), `ViewModel`, e
   `Root` + `Screen` no mesmo arquivo. A tela ganha um `<Tela>DeviceTest` em `androidDeviceTest`.
7. Presentation: `<nome>PresentationModule` com `viewModelOf(...)` e os use cases.
8. Presentation: `fun EntryProviderScope<NavKey>.<nome>Entries(callbacks...)`, a `<Nome>Key` de
   entrada pública e `val <nome>NavKeys = listOf(navKey(...))` com **todas** as keys. Key com
   argumentos recebe uma instância de exemplo: `navKey(DetailKey(id = "1"))`.
9. `:shared`: somar os módulos Koin em `appModules()`, chamar `<nome>Entries(...)` no
   `entryProvider` ligando os callbacks, somar `<nome>NavKeys` no `allNavKeys` e adicionar as
   dependências no `shared/build.gradle.kts`.

---

## 10. Lacunas conhecidas

| Item | Onde | Prioridade |
|---|---|---|
| Sem login, onboarding nem tela inicial real: o app abre na `alpha`, que é de exemplo | `:shared`, features novas | alta (primeiro passo de um projeto novo) |
| iOS: o Xcode precisa de `TEAM_ID` em `iosApp/Configuration/Config.xcconfig` para rodar em aparelho; o ícone é um placeholder liso | `iosApp` | média |
| `API_BASE_URL` é um valor de exemplo; falta BuildKonfig (dev / staging / prod) | `core:data`, `:shared` | alta, antes da primeira chamada real |
| Firebase não está configurado: faltam `google-services.json` / `GoogleService-Info.plist`, o plugin e as dependências | `:androidApp`, `iosApp` | alta, para login social e por telefone |
| Tema, cores e tipografia são placeholders, sem os tokens nem a fonte do projeto | `core:designsystem` | média |
| Ícone e splash são placeholders | `:androidApp`, `iosApp` | baixa |
| Testes de UI só no Android: o iOS não tem teste de tela | `iosApp` | baixa |

---

## 11. Stack

Todas as bibliotecas do `commonMain` são KMP. As de plataforma estão marcadas.

| Camada | Escolha | Versão |
|---|---|---|
| Linguagem | Kotlin Multiplatform | 2.4.21 |
| UI | Compose Multiplatform | 1.11.1 |
| Navegação | Navigation 3 (JetBrains) + `lifecycle-viewmodel-navigation3` | 1.1.1 / 2.11.0-beta01 |
| DI | Koin (core, compose, compose-viewmodel) | 4.2.2 |
| Rede | Ktor Client; engines OkHttp (Android) e Darwin (iOS) | 3.5.2 |
| Preferências | DataStore Preferences (KMP) | 1.2.1 |
| Banco local | Room KMP + SQLite embutido (`sqlite-bundled`), compilador via KSP | 2.8.5 / 2.7.1 / KSP 2.3.13 |
| Paginação | Paging 3 (`paging-common` no domain/data, `paging-compose` no presentation) | 3.5.3 |
| Imagens | Coil 3 (`coil-compose`) | 3.5.0 |
| Player | Media3 ExoPlayer + Session (só `androidMain` do `:core:playback`) · AVPlayer (iOS) | 1.11.1 |
| Serialização | kotlinx.serialization | 1.9.0 |
| Assíncrono | kotlinx.coroutines | 1.10.2 |
| Splash | SplashScreen API (Android) · LaunchScreen (iOS) | core-splashscreen 1.0.1 |
| Testes | kotlin-test, Turbine, AssertK, kotlinx-coroutines-test, Ktor MockEngine, Konsist | — |
| Testes em aparelho | Compose UI Test (JUnit4), AndroidX Test Runner, Espresso | 1.11.2 / 1.7.0 / 3.7.0 |
| Build | Gradle + AGP + convention plugins em `build-logic` | 9.6.0 / 9.3.0 |
| Android mínimo | minSdk 26 · compile/target 36 | — |
