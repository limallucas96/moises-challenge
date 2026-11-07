package com.lucaslima.musicsearch.core.playback.di

import org.koin.core.module.Module

/** Registra o [com.lucaslima.musicsearch.core.playback.AudioPlayer] da plataforma. */
expect val platformPlaybackModule: Module
