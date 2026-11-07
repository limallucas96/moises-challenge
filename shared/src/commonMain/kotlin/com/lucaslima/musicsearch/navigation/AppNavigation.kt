package com.lucaslima.musicsearch.navigation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.lucaslima.musicsearch.core.presentation.navigation.NavKeyRegistration
import com.lucaslima.musicsearch.core.presentation.navigation.register
import com.lucaslima.musicsearch.feature.library.presentation.navigation.AlbumKey
import com.lucaslima.musicsearch.feature.library.presentation.navigation.NowPlayingKey
import com.lucaslima.musicsearch.feature.library.presentation.navigation.SongsKey
import com.lucaslima.musicsearch.feature.library.presentation.navigation.libraryEntries
import com.lucaslima.musicsearch.feature.library.presentation.navigation.libraryNavKeys
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Todas as NavKey do app, somando a lista de cada feature. Feature nova = somar a lista dela
 * aqui. Sem registro a pilha não é restaurada (morte de processo no Android, sempre no iOS).
 */
internal val allNavKeys: List<NavKeyRegistration<*>> = libraryNavKeys

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
    val backStack = rememberNavBackStack(navConfig, SongsKey)

    // A capa da música vai da lista ao player como um elemento só.
    SharedTransitionLayout {
        NavDisplay(
            backStack = backStack,
            sharedTransitionScope = this@SharedTransitionLayout,
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                libraryEntries(
                    // Um player só na pilha: abrir de novo traz o que já existe para o topo.
                    onOpenNowPlaying = {
                        if (backStack.lastOrNull() !is NowPlayingKey) {
                            backStack.remove(NowPlayingKey)
                            backStack.add(NowPlayingKey)
                        }
                    },
                    // Só empilha se o álbum ainda não é a tela do topo (toque duplo na transição).
                    onOpenAlbum = { albumId ->
                        if (backStack.lastOrNull() != AlbumKey(albumId)) backStack.add(AlbumKey(albumId))
                    },
                    // Nunca tira a última tela: a pilha vazia fecharia o app sem passar pelo sistema.
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                    sharedTransitionScope = this@SharedTransitionLayout,
                )
            },
        )
    }
}
