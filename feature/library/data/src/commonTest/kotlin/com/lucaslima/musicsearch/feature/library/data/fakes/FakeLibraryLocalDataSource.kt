package com.lucaslima.musicsearch.feature.library.data.fakes

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.lucaslima.musicsearch.feature.library.data.local.LibraryLocalDataSource
import com.lucaslima.musicsearch.feature.library.data.local.SearchPageEntity
import com.lucaslima.musicsearch.feature.library.data.local.SongEntity
import com.lucaslima.musicsearch.feature.library.data.local.toEntity
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Banco em memória com o mesmo contrato do Room: grava, invalida o PagingSource e emite de novo. */
internal class FakeLibraryLocalDataSource : LibraryLocalDataSource {

    val searchResults = mutableMapOf<String, List<Song>>()
    val searchPages = mutableMapOf<String, SearchPageEntity>()

    /** Conta as buscas salvas: faz o fluxo da última busca emitir de novo, como o Room. */
    private val searchSaves = MutableStateFlow(0)
    private val recents = MutableStateFlow<List<Pair<Song, Instant>>>(emptyList())
    private val albums = MutableStateFlow<Map<Long, Album>>(emptyMap())
    private val sources = mutableListOf<PagingSource<Int, SongEntity>>()

    /** Quando true, toda escrita lança, como um disco cheio. */
    var failWrites = false

    val playedSongIds: List<Long> get() = recents.value.map { it.first.id }

    override fun searchPagingSource(queryKey: String): PagingSource<Int, SongEntity> =
        ListPagingSource { searchResults[queryKey].orEmpty().map { it.toEntity() } }.also { sources += it }

    override suspend fun searchPage(queryKey: String): SearchPageEntity? = searchPages[queryKey]

    override suspend fun saveSearchPage(
        queryKey: String,
        songs: List<Song>,
        replace: Boolean,
        nextPage: Int?,
        updatedAt: Instant,
    ) {
        check(!failWrites) { "disco cheio" }
        val previous = if (replace) emptyList() else searchResults[queryKey].orEmpty()
        searchResults[queryKey] = previous + songs
        searchPages[queryKey] = SearchPageEntity(queryKey, nextPage, updatedAt.toEpochMilliseconds())
        searchSaves.value++
        sources.forEach { it.invalidate() }
        sources.clear()
    }

    override fun observeRecentlyPlayed(limit: Int): Flow<List<Song>> =
        recents.map { entries -> entries.sortedByDescending { it.second }.take(limit).map { it.first } }

    override fun observeLatestSearchResults(limit: Int): Flow<List<Song>> = searchSaves.map {
        val latest = searchPages.values.maxByOrNull { it.updatedAtMillis }
        latest?.let { searchResults[it.queryKey].orEmpty().take(limit) }.orEmpty()
    }

    override suspend fun markPlayed(
        song: Song,
        playedAt: Instant,
    ) {
        check(!failWrites) { "disco cheio" }
        recents.value = recents.value.filterNot { it.first.id == song.id } + (song to playedAt)
    }

    override fun observeAlbum(albumId: Long): Flow<Album?> = albums.map { it[albumId] }

    override suspend fun saveAlbum(
        album: Album,
        updatedAt: Instant,
    ) {
        check(!failWrites) { "disco cheio" }
        albums.value += album.id to album
    }
}

/** PagingSource por posição sobre uma lista lida no momento do load. */
private class ListPagingSource(private val items: () -> List<SongEntity>) : PagingSource<Int, SongEntity>() {
    override fun getRefreshKey(state: PagingState<Int, SongEntity>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SongEntity> {
        val all = items()
        val start = params.key ?: 0
        val page = all.drop(start).take(params.loadSize)
        val next = start + page.size
        return LoadResult.Page(
            data = page,
            prevKey = null,
            nextKey = if (next < all.size) next else null,
        )
    }
}
