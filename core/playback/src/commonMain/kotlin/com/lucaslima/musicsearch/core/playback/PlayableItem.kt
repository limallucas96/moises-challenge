package com.lucaslima.musicsearch.core.playback

/**
 * O que o player sabe tocar. Não conhece o modelo de nenhuma feature: quem chama converte o
 * próprio modelo para este.
 */
data class PlayableItem(
    val id: String,
    val title: String,
    val artist: String,
    val albumTitle: String,
    val artworkUrl: String?,
    val audioUrl: String,
)
