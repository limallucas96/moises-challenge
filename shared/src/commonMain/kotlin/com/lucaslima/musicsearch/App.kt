package com.lucaslima.musicsearch

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lucaslima.musicsearch.core.designsystem.theme.AppTheme
import com.lucaslima.musicsearch.navigation.AppNavigation

/** Raiz do app, compartilhada entre Android e iOS. */
@Composable
fun App(modifier: Modifier = Modifier) {
    AppTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = AppTheme.colors.bg,
        ) {
            AppNavigation()
        }
    }
}
