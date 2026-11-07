package com.lucaslima.musicsearch.wear

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.TimeText
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.lucaslima.musicsearch.wear.navigation.WearNavigation

/** Raiz do app do relógio: tema escuro padrão do Wear, a hora no topo e a navegação. */
@Composable
fun WearApp() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
    MaterialTheme {
        AppScaffold(timeText = { TimeText() }) {
            WearNavigation()
        }
    }
}
