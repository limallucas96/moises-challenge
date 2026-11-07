package com.lucaslima.musicsearch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.navigation.AppNavigation
import com.lucaslima.musicsearch.splash.AppSplash

/** Raiz do app, compartilhada entre Android e iOS. */
@Composable
fun App(modifier: Modifier = Modifier) {
    // Capas pela rede com o Ktor (o mesmo motor do app nas duas plataformas), com cache em
    // memória e em disco do próprio Coil.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
    AppTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = AppTheme.colors.bg,
        ) {
            Box {
                AppNavigation()
                AppSplash()
            }
        }
    }
}
