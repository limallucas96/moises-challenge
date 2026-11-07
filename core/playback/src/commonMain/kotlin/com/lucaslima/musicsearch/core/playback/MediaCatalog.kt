package com.lucaslima.musicsearch.core.playback

import kotlinx.coroutines.flow.Flow

/**
 * O que dá para tocar sem abrir o app: as listas que o Android Auto mostra e a busca por voz
 * ("toque Daft Punk"). A feature implementa; o serviço de mídia só monta a árvore com isto.
 */
interface MediaCatalog {
    /** Seções da raiz, na ordem. No Auto viram abas. */
    fun sections(): List<CatalogSection>

    /** Músicas tocáveis da seção. Vazio quando a seção não existe ou não tem nada. */
    suspend fun items(sectionId: String): List<PlayableItem>

    /**
     * O id de cada seção cujo conteúdo mudou (pode emitir sem mudança real). O Auto guarda a
     * lista que já carregou: sem este aviso, uma seção vista vazia continuaria vazia.
     */
    fun sectionChanges(): Flow<String>

    /** Busca por texto ou voz. Vazio quando não acha nada ou a rede falha. */
    suspend fun search(query: String): List<PlayableItem>
}

data class CatalogSection(
    val id: String,
    val title: String,
)
