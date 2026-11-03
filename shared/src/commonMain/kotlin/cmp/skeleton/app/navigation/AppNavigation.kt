package cmp.skeleton.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import cmp.skeleton.app.core.presentation.navigation.NavKeyRegistration
import cmp.skeleton.app.core.presentation.navigation.register
import cmp.skeleton.app.feature.alpha.presentation.navigation.AlphaKey
import cmp.skeleton.app.feature.alpha.presentation.navigation.alphaEntries
import cmp.skeleton.app.feature.alpha.presentation.navigation.alphaNavKeys
import cmp.skeleton.app.feature.beta.presentation.navigation.BetaKey
import cmp.skeleton.app.feature.beta.presentation.navigation.betaEntries
import cmp.skeleton.app.feature.beta.presentation.navigation.betaNavKeys
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Todas as NavKey do app, somando a lista de cada feature. Feature nova = somar a lista dela
 * aqui. Sem registro a pilha não é restaurada (morte de processo no Android, sempre no iOS).
 */
internal val allNavKeys: List<NavKeyRegistration<*>> = alphaNavKeys + betaNavKeys

internal val navSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        register(allNavKeys)
    }
}

internal val navConfig = SavedStateConfiguration {
    serializersModule = navSerializersModule
}

/** Único lugar que conhece mais de uma feature: dono da pilha e da fiação entre elas. */
@Composable
internal fun AppNavigation() {
    val backStack = rememberNavBackStack(navConfig, AlphaKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            alphaEntries(
                // Só empilha se a Alpha ainda é a tela do topo: um segundo toque durante a
                // transição não abre a Beta duas vezes.
                onOpenBetaClick = { message ->
                    if (backStack.lastOrNull() is AlphaKey) backStack.add(BetaKey(message))
                },
            )
            betaEntries(
                // Só sai se a Beta ainda está no topo: senão tiraria a tela de baixo da pilha.
                onBackClick = { if (backStack.lastOrNull() is BetaKey) backStack.removeLastOrNull() },
            )
        },
    )
}
