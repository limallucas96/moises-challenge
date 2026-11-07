package com.lucaslima.musicsearch.feature.library.presentation.nowplaying

import coil3.request.ImageRequest

/** No iOS o bitmap do Coil já permite ler pixel. */
internal actual fun ImageRequest.Builder.allowPixelReads(): ImageRequest.Builder = this
