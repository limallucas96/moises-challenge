package com.lucaslima.musicsearch.feature.library.presentation.nowplaying

import coil3.request.ImageRequest
import coil3.request.allowHardware

internal actual fun ImageRequest.Builder.allowPixelReads(): ImageRequest.Builder = allowHardware(false)
