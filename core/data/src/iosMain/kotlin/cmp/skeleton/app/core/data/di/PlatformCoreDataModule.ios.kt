@file:OptIn(ExperimentalForeignApi::class)

package cmp.skeleton.app.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import cmp.skeleton.app.core.data.storage.PREFERENCES_FILE_NAME
import cmp.skeleton.app.core.data.storage.createPreferencesDataStore
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual val platformCoreDataModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single<DataStore<Preferences>> {
        createPreferencesDataStore {
            val documents = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            requireNotNull(documents?.path) + "/" + PREFERENCES_FILE_NAME
        }
    }
}
