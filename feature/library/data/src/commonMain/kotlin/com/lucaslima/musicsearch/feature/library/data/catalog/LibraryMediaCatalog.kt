package com.lucaslima.musicsearch.feature.library.data.catalog

import com.lucaslima.musicsearch.core.domain.Result
import com.lucaslima.musicsearch.core.playback.CatalogSection
import com.lucaslima.musicsearch.core.playback.MediaCatalog
import com.lucaslima.musicsearch.core.playback.PlayableItem
import com.lucaslima.musicsearch.feature.library.data.local.LibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.player.KnownSongs
import com.lucaslima.musicsearch.feature.library.data.player.toPlayableItem
import com.lucaslima.musicsearch.feature.library.data.remote.CatalogRemoteDataSource
import com.lucaslima.musicsearch.feature.library.data.repository.toQueryKey
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.coroutines.flow.first

/**
 * O catálogo da biblioteca para quem toca de fora do app (Android Auto, voz). Lê o banco
 * (recentes e a última busca) e busca na rede como a tela de busca, salvando o resultado: a
 * busca feita no carro aparece em "Last search" e fica disponível offline.
 *
 * Os títulos das seções ficam aqui, sem tradução: o app inteiro é só em inglês.
 */
internal class LibraryMediaCatalog(
    private val remote: CatalogRemoteDataSource,
    private val local: LibraryLocalDataSource,
    private val knownSongs: KnownSongs,
    private val clock: Clock,
    private val limit: Int = ITEM_LIMIT,
) : MediaCatalog {

    override fun sections(): List<CatalogSection> = listOf(
        CatalogSection(id = RECENT, title = "Recently played"),
        CatalogSection(id = LAST_SEARCH, title = "Last search"),
    )

    override suspend fun items(sectionId: String): List<PlayableItem> {
        val songs = when (sectionId) {
            RECENT -> local.observeRecentlyPlayed(limit).first()
            LAST_SEARCH -> local.latestSearchResults(limit)
            else -> emptyList()
        }
        return songs.toPlayable()
    }

    override suspend fun search(query: String): List<PlayableItem> {
        val term = query.trim()
        if (term.isEmpty()) return emptyList()
        val page = when (val result = remote.searchSongs(term, page = 0, pageSize = limit)) {
            is Result.Error -> return emptyList()
            is Result.Success -> result.data
        }
        try {
            local.saveSearchPage(
                queryKey = term.toQueryKey(),
                songs = page.songs,
                replace = true,
                nextPage = if (page.endReached) null else 1,
                updatedAt = clock.now(),
            )
        } catch (e: Exception) {
            // Sem cache a busca ainda serve: só não fica salva.
            if (e is CancellationException) throw e
        }
        return page.songs.toPlayable()
    }

    /** Só o que tem prévia de áudio entra; o player ganha a [Song] de volta pelo id. */
    private fun List<Song>.toPlayable(): List<PlayableItem> {
        val playable = filter { it.previewUrl != null }
        knownSongs.remember(playable)
        return playable.map { it.toPlayableItem() }
    }

    companion object {
        const val RECENT = "recent"
        const val LAST_SEARCH = "last-search"
        private const val ITEM_LIMIT = 25
    }
}
