/*
 * Copyright (c) 2024. Marcel van Heerwaarden
 *
 * Copyright (C) 2019 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mheerwaarden.dynamictheme.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.mheerwaarden.dynamictheme.APP_TAG
import dynamiccolor.Variant
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val TAG = APP_TAG + "UserPreferencesRepo"

class UserPreferencesDataStoreRepository(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {
    override suspend fun saveIdPreference(id: Long): Preferences = dataStore.edit { settings ->
        // Set id and reset the other values to default
        val defaultPreferences = UserPreferences()
        settings[ID] = id
        settings[NAME] = defaultPreferences.name
        settings[SOURCE_COLOR] = defaultPreferences.sourceColor
        settings[COLOR_SCHEME_VARIANT] = defaultPreferences.dynamicSchemeVariant.ordinal
    }

    override suspend fun saveNamePreference(name: String): Preferences =
            dataStore.edit { settings -> settings[NAME] = name }

    override suspend fun saveSourceColorPreference(
        color: Int,
        colorSchemeVariant: Variant,
    ): Preferences = dataStore.edit { settings ->
        settings[SOURCE_COLOR] = color
        settings[COLOR_SCHEME_VARIANT] = colorSchemeVariant.ordinal
    }

    @OptIn(FlowPreview::class)
    override val preferences: Flow<UserPreferences> = dataStore.data
        // Do not update while the user is typing
        .debounce(300)
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { settings ->
            Log.d(TAG, "UserPreferencesDataStoreRepository: settings = $settings")
            val defaultPreferences = UserPreferences()
            UserPreferences(
                id = settings[ID] ?: defaultPreferences.id,
                name = settings[NAME] ?: defaultPreferences.name,
                sourceColor = settings[SOURCE_COLOR] ?: defaultPreferences.sourceColor,
                dynamicSchemeVariant = Variant.entries[settings[COLOR_SCHEME_VARIANT]
                        ?: defaultPreferences.dynamicSchemeVariant.ordinal]
            )
        }

    private companion object {
        val ID = longPreferencesKey("id")
        val NAME = stringPreferencesKey("name")
        val SOURCE_COLOR = intPreferencesKey("source_color")
        val COLOR_SCHEME_VARIANT = intPreferencesKey("color_scheme_variant")
    }
}
