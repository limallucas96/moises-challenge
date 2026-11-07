package com.lucaslima.musicsearch.core.playback

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

/**
 * A árvore que o Android Auto navega: raiz, uma pasta por seção do [MediaCatalog] e as músicas
 * dela. O id de cada nó diz de onde ele veio, para que tocar uma música toque a seção inteira
 * a partir dela.
 *
 * Sem catálogo (nenhum registrado no Koin), a árvore só tem a raiz.
 */
internal class MediaTree(private val catalog: MediaCatalog?) {

    /** Última busca do Auto, para montar a fila quando o motorista toca num resultado. */
    private var searchResults: List<PlayableItem> = emptyList()

    val root: MediaItem = folder(id = ROOT_ID, title = "Music Search")

    fun sections(): List<MediaItem> =
        catalog?.sections().orEmpty().map { folder(id = sectionId(it.id), title = it.title) }

    /** O id do nó de cada seção que mudou, para avisar quem está navegando. */
    fun sectionChanges(): Flow<String> = catalog?.sectionChanges()?.map { sectionId(it) } ?: emptyFlow()

    suspend fun children(parentId: String): List<MediaItem> = when {
        parentId == ROOT_ID -> sections()

        parentId.startsWith(SECTION_PREFIX) -> {
            val section = parentId.removePrefix(SECTION_PREFIX)
            catalog?.items(section).orEmpty().map { it.toBrowsableChild(itemId(section, it.id)) }
        }

        else -> emptyList()
    }

    suspend fun search(query: String): List<MediaItem> {
        searchResults = catalog?.search(query).orEmpty()
        return searchResults.map { it.toBrowsableChild(itemId(SEARCH_SECTION, it.id)) }
    }

    suspend fun item(mediaId: String): MediaItem? = when {
        mediaId == ROOT_ID -> root
        mediaId.startsWith(SECTION_PREFIX) -> sections().firstOrNull { it.mediaId == mediaId }
        else -> queueFor(mediaId)?.let { it.items[it.startIndex].toMediaItem() }
    }

    /**
     * A fila para um nó tocado no Auto: a seção (ou a busca) inteira, começando na música
     * escolhida. Null quando o id não é de música da árvore.
     */
    suspend fun queueFor(mediaId: String): Queue? {
        val (section, songId) = parseItemId(mediaId) ?: return null
        val items = if (section == SEARCH_SECTION) searchResults else catalog?.items(section).orEmpty()
        val index = items.indexOfFirst { it.id == songId }
        if (index < 0) return null
        val title = catalog?.sections()?.firstOrNull { it.id == section }?.title
        return Queue(items, index, title)
    }

    /** "Toque Daft Punk": os resultados da busca viram a fila, a partir do primeiro. */
    suspend fun queueForVoice(query: String): Queue? {
        val items = catalog?.search(query).orEmpty()
        if (items.isEmpty()) return null
        searchResults = items
        return Queue(items, startIndex = 0, title = query)
    }

    /** Retomar pelo carro ou fone Bluetooth sem fila: começa pela primeira seção (recentes). */
    suspend fun resumptionQueue(): Queue? {
        val section = catalog?.sections()?.firstOrNull() ?: return null
        val items = catalog.items(section.id)
        return if (items.isEmpty()) null else Queue(items, startIndex = 0, title = section.title)
    }

    data class Queue(
        val items: List<PlayableItem>,
        val startIndex: Int,
        val title: String?,
    )

    companion object {
        const val ROOT_ID = "root"
        private const val SECTION_PREFIX = "section|"
        private const val ITEM_PREFIX = "item|"
        private const val SEARCH_SECTION = "search"

        fun sectionId(section: String) = SECTION_PREFIX + section

        fun itemId(
            section: String,
            itemId: String,
        ) = "$ITEM_PREFIX$section|$itemId"

        fun parseItemId(mediaId: String): Pair<String, String>? {
            if (!mediaId.startsWith(ITEM_PREFIX)) return null
            val parts = mediaId.removePrefix(ITEM_PREFIX).split('|', limit = 2)
            return if (parts.size == 2) parts[0] to parts[1] else null
        }

        private fun folder(
            id: String,
            title: String,
        ): MediaItem = MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                    .build(),
            )
            .build()

        /** O mesmo item tocável, com o id do nó da árvore. */
        private fun PlayableItem.toBrowsableChild(nodeId: String): MediaItem =
            toMediaItem().buildUpon().setMediaId(nodeId).build()
    }
}
