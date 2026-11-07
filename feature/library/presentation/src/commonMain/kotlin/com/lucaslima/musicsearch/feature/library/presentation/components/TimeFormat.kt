package com.lucaslima.musicsearch.feature.library.presentation.components

import kotlin.time.Duration

/** "1:26", ou "1:02:03" a partir de uma hora. Negativo vira zero. */
internal fun Duration.toClock(): String {
    val total = inWholeSeconds.coerceAtLeast(0)
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = (total % 60).toString().padStart(2, '0')
    return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}:$seconds" else "$minutes:$seconds"
}
