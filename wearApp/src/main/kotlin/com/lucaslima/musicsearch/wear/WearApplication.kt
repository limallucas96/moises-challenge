package com.lucaslima.musicsearch.wear

import android.app.Application
import com.lucaslima.musicsearch.wear.di.wearModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WearApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WearApplication)
            modules(wearModules())
        }
    }
}
