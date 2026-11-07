package com.lucaslima.musicsearch.feature.library.domain

/** Um álbum e as faixas dele, na ordem do disco. */
data class Album(
    val id: Long,
    val title: String,
    val artistName: String,
    val artworkUrl: String?,
    val songs: List<Song>,
)
