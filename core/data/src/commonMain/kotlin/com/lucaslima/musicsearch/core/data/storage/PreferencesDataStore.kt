package com.lucaslima.musicsearch.core.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Um único DataStore de preferências para o app (flags, sessão). Leitura e escrita
 * são suspend e rodam fora da main thread. Dado de domínio vai para o Room, não para cá.
 *
 * TODO(segurança): DataStore não criptografa. Token de sessão deve ir para Keychain (iOS)
 * e armazenamento criptografado (Android) antes de produção.
 */
internal const val PREFERENCES_FILE_NAME = "app.preferences_pb"

internal fun createPreferencesDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
