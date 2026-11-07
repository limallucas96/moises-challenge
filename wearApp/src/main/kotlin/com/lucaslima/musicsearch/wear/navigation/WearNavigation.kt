package com.lucaslima.musicsearch.wear.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import com.lucaslima.musicsearch.core.presentation.navigation.NavKeyRegistration
import com.lucaslima.musicsearch.core.presentation.navigation.navKey
import com.lucaslima.musicsearch.core.presentation.navigation.register
import com.lucaslima.musicsearch.wear.ui.AlbumListRoot
import com.lucaslima.musicsearch.wear.ui.MenuRoot
import com.lucaslima.musicsearch.wear.ui.PlayerRoot
import com.lucaslima.musicsearch.wear.ui.SongListRoot
import com.lucaslima.musicsearch.wear.ui.TrackListRoot
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/** Menu "Music": Now playing, Songs e Albums. */
@Serializable
data object MenuKey : NavKey

@Serializable
data object PlayerKey : NavKey

/** Recentes e busca por voz ou teclado. */
@Serializable
data object SongListKey : NavKey

/** Álbuns das músicas tocadas há pouco. */
@Serializable
data object AlbumListKey : NavKey

/** As faixas de um álbum. */
@Serializable
data class TrackListKey(val albumId: Long) : NavKey

internal val wearNavKeys: List<NavKeyRegistration<*>> = listOf(
    navKey(MenuKey),
    navKey(PlayerKey),
    navKey(SongListKey),
    navKey(AlbumListKey),
    navKey(TrackListKey(albumId = 1)),
)

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) { register(wearNavKeys) }
    }
}

/** A pilha do relógio. Voltar é deslizar da esquerda para a direita, como em todo app Wear. */
@Composable
internal fun WearNavigation() {
    val backStack = rememberNavBackStack(navConfig, MenuKey)
    // Um player só na pilha, como no celular.
    val openPlayer = {
        if (backStack.lastOrNull() !is PlayerKey) {
            backStack.remove(PlayerKey)
            backStack.add(PlayerKey)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        sceneStrategies = listOf(rememberSwipeDismissableSceneStrategy()),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<MenuKey> {
                MenuRoot(
                    onOpenPlayer = openPlayer,
                    onOpenSongs = { backStack.add(SongListKey) },
                    onOpenAlbums = { backStack.add(AlbumListKey) },
                )
            }
            entry<PlayerKey> { PlayerRoot() }
            entry<SongListKey> { SongListRoot(onOpenPlayer = openPlayer) }
            entry<AlbumListKey> { AlbumListRoot(onOpenAlbum = { backStack.add(TrackListKey(it)) }) }
            entry<TrackListKey> { key -> TrackListRoot(albumId = key.albumId, onOpenPlayer = openPlayer) }
        },
    )
}
