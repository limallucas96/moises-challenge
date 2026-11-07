package com.lucaslima.musicsearch.feature.library.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
internal data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artistName: String,
    val albumId: Long,
    val albumTitle: String,
    val artworkUrl: String?,
    val previewUrl: String?,
    val durationMillis: Long,
    val trackNumber: Int?,
)
