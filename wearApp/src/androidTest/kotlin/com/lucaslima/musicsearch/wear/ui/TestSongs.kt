package com.lucaslima.musicsearch.wear.ui

import androidx.annotation.StringRes
import androidx.test.platform.app.InstrumentationRegistry
import com.lucaslima.musicsearch.feature.library.domain.Song
import kotlin.time.Duration.Companion.seconds

internal fun song(
    id: Long,
    title: String = "Song $id",
    albumId: Long = 100,
    albumTitle: String = "Album $albumId",
) = Song(
    id = id,
    title = title,
    artistName = "Daft Punk",
    albumId = albumId,
    albumTitle = albumTitle,
    artworkUrl = null,
    previewUrl = "https://audio.example/$id.m4a",
    duration = 30.seconds,
    trackNumber = id.toInt(),
)

/** As strings do app do relógio são recursos Android comuns. */
internal fun text(@StringRes id: Int): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
