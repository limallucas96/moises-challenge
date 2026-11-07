package com.lucaslima.musicsearch.feature.library.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class SongDao {

    @Upsert
    abstract suspend fun upsertSongs(songs: List<SongEntity>)

    // --- Busca ---

    @Query(
        """
        SELECT songs.* FROM search_results
        INNER JOIN songs ON songs.id = search_results.songId
        WHERE search_results.queryKey = :queryKey
        ORDER BY search_results.position
        """,
    )
    abstract fun searchPagingSource(queryKey: String): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM search_pages WHERE queryKey = :queryKey")
    abstract suspend fun searchPage(queryKey: String): SearchPageEntity?

    @Query("SELECT COUNT(*) FROM search_results WHERE queryKey = :queryKey")
    abstract suspend fun searchResultCount(queryKey: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSearchResults(results: List<SearchResultEntity>)

    @Upsert
    abstract suspend fun upsertSearchPage(page: SearchPageEntity)

    @Query("DELETE FROM search_results WHERE queryKey = :queryKey")
    abstract suspend fun clearSearchResults(queryKey: String)

    /** Grava uma página inteira de uma vez: quem observa a busca nunca vê a página pela metade. */
    @Transaction
    open suspend fun saveSearchPage(
        queryKey: String,
        songs: List<SongEntity>,
        replace: Boolean,
        nextPage: Int?,
        updatedAtMillis: Long,
    ) {
        if (replace) clearSearchResults(queryKey)
        val start = searchResultCount(queryKey)
        upsertSongs(songs)
        insertSearchResults(
            songs.mapIndexed { index, song -> SearchResultEntity(queryKey, start + index, song.id) },
        )
        upsertSearchPage(SearchPageEntity(queryKey, nextPage, updatedAtMillis))
    }

    @Query(
        """
        SELECT songs.* FROM search_results
        INNER JOIN songs ON songs.id = search_results.songId
        WHERE search_results.queryKey =
            (SELECT queryKey FROM search_pages ORDER BY updatedAtMillis DESC LIMIT 1)
        ORDER BY search_results.position
        LIMIT :limit
        """,
    )
    abstract fun observeLatestSearchResults(limit: Int): Flow<List<SongEntity>>

    // --- Recentes ---

    @Query(
        """
        SELECT songs.* FROM recently_played
        INNER JOIN songs ON songs.id = recently_played.songId
        ORDER BY recently_played.playedAtMillis DESC
        LIMIT :limit
        """,
    )
    abstract fun observeRecentlyPlayed(limit: Int): Flow<List<SongEntity>>

    @Upsert
    abstract suspend fun upsertRecentlyPlayed(entry: RecentlyPlayedEntity)

    @Transaction
    open suspend fun markPlayed(
        song: SongEntity,
        playedAtMillis: Long,
    ) {
        upsertSongs(listOf(song))
        upsertRecentlyPlayed(RecentlyPlayedEntity(song.id, playedAtMillis))
    }

    // --- Álbum ---

    @Transaction
    @Query("SELECT * FROM albums WHERE id = :albumId")
    abstract fun observeAlbum(albumId: Long): Flow<AlbumWithSongs?>

    @Upsert
    abstract suspend fun upsertAlbum(album: AlbumEntity)

    @Transaction
    open suspend fun saveAlbum(
        album: AlbumEntity,
        songs: List<SongEntity>,
    ) {
        upsertSongs(songs)
        upsertAlbum(album)
    }
}
