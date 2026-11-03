package cmp.skeleton.app

import android.app.Application
import cmp.skeleton.app.di.initKoin
import org.koin.android.ext.koin.androidContext

class CmpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@CmpApplication)
        }
    }
}
