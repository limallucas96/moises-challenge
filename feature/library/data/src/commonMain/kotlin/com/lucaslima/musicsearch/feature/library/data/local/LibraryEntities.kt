package com.lucaslima.musicsearch.feature.library.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** Uma música. Compartilhada por busca, recentes e álbum: cada música é salva uma vez só. */
@Entity(tableName = "songs", indices = [Index("albumId")])
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

/** A posição de uma música no resultado de uma busca. A ordem vem da API. */
@Entity(
    tableName = "search_results",
    primaryKeys = ["queryKey", "position"],
    foreignKeys = [ForeignKey(SongEntity::class, ["id"], ["songId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("songId")],
)
internal data class SearchResultEntity(
    val queryKey: String,
    val position: Int,
    val songId: Long,
)

/** Estado da paginação de uma busca: próxima página e quando foi atualizada. */
@Entity(tableName = "search_pages")
internal data class SearchPageEntity(
    @PrimaryKey val queryKey: String,
    /** Null quando a última página já foi carregada. */
    val nextPage: Int?,
    val updatedAtMillis: Long,
)

@Entity(
    tableName = "recently_played",
    foreignKeys = [ForeignKey(SongEntity::class, ["id"], ["songId"], onDelete = ForeignKey.CASCADE)],
)
internal data class RecentlyPlayedEntity(
    @PrimaryKey val songId: Long,
    val playedAtMillis: Long,
)

/** Cabeçalho do álbum. As faixas são as [SongEntity] com o mesmo `albumId`. */
@Entity(tableName = "albums")
internal data class AlbumEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artistName: String,
    val artworkUrl: String?,
    val updatedAtMillis: Long,
)

/** Álbum e faixas lidos numa consulta só: quem observa nunca vê o cabeçalho sem as faixas. */
internal data class AlbumWithSongs(
    @Embedded val album: AlbumEntity,
    @Relation(parentColumn = "id", entityColumn = "albumId")
    val songs: List<SongEntity>,
)
