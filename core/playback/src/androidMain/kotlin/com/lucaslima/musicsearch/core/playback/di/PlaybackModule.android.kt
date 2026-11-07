package com.lucaslima.musicsearch.core.playback.di

import com.lucaslima.musicsearch.core.playback.AudioPlayer
import com.lucaslima.musicsearch.core.playback.Media3AudioPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPlaybackModule: Module = module {
    // O Context vem do androidContext() que a Application passa ao Koin.
    single<AudioPlayer> {
        Media3AudioPlayer(context = get(), scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate))
    }
}
