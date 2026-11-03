package cmp.skeleton.app.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import cmp.skeleton.app.core.data.storage.PREFERENCES_FILE_NAME
import cmp.skeleton.app.core.data.storage.createPreferencesDataStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module

// O Context vem do androidContext(...) registrado no :androidApp.
internal actual val platformCoreDataModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<DataStore<Preferences>> {
        val context = get<Context>()
        createPreferencesDataStore { context.filesDir.resolve(PREFERENCES_FILE_NAME).absolutePath }
    }
}
