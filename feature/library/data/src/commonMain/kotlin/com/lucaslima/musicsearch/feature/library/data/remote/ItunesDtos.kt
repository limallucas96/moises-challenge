package com.lucaslima.musicsearch.feature.library.data.remote

import kotlinx.serialization.Serializable

/** Resposta de `/search` e `/lookup`: os dois endpoints usam o mesmo envelope. */
@Serializable
internal data class ItunesResponseDto(
    val resultCount: Int = 0,
    val results: List<ItunesItemDto> = emptyList(),
)

/**
 * Um item da resposta. O `/lookup` de álbum mistura o próprio álbum
 * (`wrapperType = collection`) com as faixas (`wrapperType = track`), por isso quase tudo é
 * opcional.
 */
@Serializable
internal data class ItunesItemDto(
    val wrapperType: String? = null,
    val kind: String? = null,
    val trackId: Long? = null,
    val collectionId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val collectionName: String? = null,
    val artworkUrl100: String? = null,
    val previewUrl: String? = null,
    val trackTimeMillis: Long? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
)
