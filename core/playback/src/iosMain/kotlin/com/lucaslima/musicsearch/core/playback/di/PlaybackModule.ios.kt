package com.lucaslima.musicsearch.core.playback.di

import com.lucaslima.musicsearch.core.playback.AudioPlayer
import com.lucaslima.musicsearch.core.playback.AvAudioPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformPlaybackModule: Module = module {
    single<AudioPlayer> { AvAudioPlayer(scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)) }
}
