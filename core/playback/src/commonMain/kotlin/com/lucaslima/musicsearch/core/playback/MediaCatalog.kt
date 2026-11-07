package com.lucaslima.musicsearch.core.playback

/**
 * O que dá para tocar sem abrir o app: as listas que o Android Auto mostra e a busca por voz
 * ("toque Daft Punk"). A feature implementa; o serviço de mídia só monta a árvore com isto.
 */
interface MediaCatalog {
    /** Seções da raiz, na ordem. No Auto viram abas. */
    fun sections(): List<CatalogSection>

    /** Músicas tocáveis da seção. Vazio quando a seção não existe ou não tem nada. */
    suspend fun items(sectionId: String): List<PlayableItem>

    /** Busca por texto ou voz. Vazio quando não acha nada ou a rede falha. */
    suspend fun search(query: String): List<PlayableItem>
}

data class CatalogSection(
    val id: String,
    val title: String,
)
