package cmp.skeleton.app.di

import cmp.skeleton.app.core.data.di.coreDataModule
import cmp.skeleton.app.feature.alpha.data.di.alphaDataModule
import cmp.skeleton.app.feature.alpha.presentation.di.alphaPresentationModule
import cmp.skeleton.app.feature.beta.presentation.di.betaPresentationModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatformTools

// TODO(config): mover para BuildKonfig por ambiente (dev / staging / prod) quando o app chamar a API.
private const val API_BASE_URL = "https://api.example.dev/"

fun appModules(): List<Module> = listOf(
    coreDataModule(API_BASE_URL),
    alphaDataModule,
    alphaPresentationModule,
    betaPresentationModule,
)

/** Idempotente: pode ser chamado de novo sem reiniciar o Koin. */
fun initKoin(config: KoinAppDeclaration? = null) {
    // GlobalContext só existe na JVM; o contexto padrão comum vem do KoinPlatformTools.
    if (KoinPlatformTools.defaultContext().getOrNull() != null) return
    startKoin {
        config?.invoke(this)
        modules(appModules())
    }
}
