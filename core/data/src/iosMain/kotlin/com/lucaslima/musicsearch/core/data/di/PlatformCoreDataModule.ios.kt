@file:OptIn(ExperimentalForeignApi::class)

package com.lucaslima.musicsearch.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.lucaslima.musicsearch.core.data.storage.PREFERENCES_FILE_NAME
import com.lucaslima.musicsearch.core.data.storage.createPreferencesDataStore
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
