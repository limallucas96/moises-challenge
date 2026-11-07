package com.lucaslima.musicsearch.feature.library.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lucaslima.musicsearch.core.presentation.navigation.NavKeyRegistration
import com.lucaslima.musicsearch.core.presentation.navigation.navKey
import com.lucaslima.musicsearch.feature.library.presentation.songs.SongsRoot
import kotlinx.serialization.Serializable

/** Key de entrada da feature: a lista de músicas (Home). */
@Serializable
data object SongsKey : NavKey

fun EntryProviderScope<NavKey>.libraryEntries() {
    entry<SongsKey> { SongsRoot() }
}

/** TODAS as NavKey da feature. Key nova = uma linha nova aqui. */
val libraryNavKeys: List<NavKeyRegistration<*>> = listOf(
    navKey(SongsKey),
)
