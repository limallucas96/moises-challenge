package com.lucaslima.musicsearch.feature.library.data.player

import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * As músicas que já foram entregues ao player, pelo id do item tocável. O player só devolve o
 * id do que está tocando; isto o traduz de volta para a [Song], venha a fila do app ou do Auto.
 */
internal class KnownSongs {
    private val _songs = MutableStateFlow<Map<String, Song>>(emptyMap())
    val songs: StateFlow<Map<String, Song>> = _songs.asStateFlow()

    fun remember(songs: List<Song>) {
        if (songs.isEmpty()) return
        _songs.update { known -> known + songs.associateBy { it.id.toString() } }
    }
}
