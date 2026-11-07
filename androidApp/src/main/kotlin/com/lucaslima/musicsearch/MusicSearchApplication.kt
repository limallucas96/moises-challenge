package com.lucaslima.musicsearch

import android.app.Application
import com.lucaslima.musicsearch.di.initKoin
import org.koin.android.ext.koin.androidContext

class MusicSearchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MusicSearchApplication)
        }
    }
}
