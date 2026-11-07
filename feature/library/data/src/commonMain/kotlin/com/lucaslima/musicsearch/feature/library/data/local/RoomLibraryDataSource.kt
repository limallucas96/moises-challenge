package com.lucaslima.musicsearch.feature.library.data.local

import androidx.paging.PagingSource
import com.lucaslima.musicsearch.feature.library.domain.Album
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class RoomLibraryDataSource(private val dao: SongDao) : LibraryLocalDataSource {

    override fun searchPagingSource(queryKey: String): PagingSource<Int, SongEntity> = dao.searchPagingSource(queryKey)

    override suspend fun searchPage(queryKey: String): SearchPageEntity? = dao.searchPage(queryKey)

    override suspend fun saveSearchPage(
        queryKey: String,
        songs: List<Song>,
        replace: Boolean,
        nextPage: Int?,
        updatedAt: Instant,
    ) = dao.saveSearchPage(
        queryKey = queryKey,
        songs = songs.map { it.toEntity() },
        replace = replace,
        nextPage = nextPage,
        updatedAtMillis = updatedAt.toEpochMilliseconds(),
    )

    override fun observeRecentlyPlayed(limit: Int): Flow<List<Song>> = dao.observeRecentlyPlayed(limit)
        .map { songs -> songs.map { it.toSong() } }
        .distinctUntilChanged()

    override suspend fun latestSearchResults(limit: Int): List<Song> = dao.latestSearchResults(limit).map {
        it.toSong()
    }

    override suspend fun markPlayed(
        song: Song,
        playedAt: Instant,
    ) = dao.markPlayed(song.toEntity(), playedAt.toEpochMilliseconds())

    override fun observeAlbum(albumId: Long): Flow<Album?> = dao.observeAlbum(albumId)
        .map { it?.album?.toAlbum(it.songs) }
        .distinctUntilChanged()

    override suspend fun saveAlbum(
        album: Album,
        updatedAt: Instant,
    ) = dao.saveAlbum(album.toEntity(updatedAt.toEpochMilliseconds()), album.songs.map { it.toEntity() })
}
