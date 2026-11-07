package com.lucaslima.musicsearch.feature.library.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
internal interface SongDao {
    @Upsert
    suspend fun upsert(songs: List<SongEntity>)

    @Query("SELECT * FROM songs ORDER BY title")
    fun pagingSource(): PagingSource<Int, SongEntity>
}
