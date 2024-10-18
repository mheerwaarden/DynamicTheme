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
import com.github.mheerwaarden.dynamictheme.APP_TAG
import dynamiccolor.Variant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val TAG = APP_TAG + "UserPreferencesRepo"

class UserPreferencesDataStoreRepository(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {
    override suspend fun saveSourceColorPreference(color: Int, colorSchemeVariant: Variant) {
        dataStore.edit { preferences ->
            preferences[SOURCE_COLOR] = color
            preferences[COLOR_SCHEME_VARIANT] = colorSchemeVariant.ordinal
        }
    }

    override val preferences: Flow<UserPreferences> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { settings ->
            Log.d(TAG, "preferences: $settings")
            UserPreferences(
                sourceColor = settings[SOURCE_COLOR] ?: UserPreferences().sourceColor,
                dynamicSchemeVariant = Variant.entries[settings[COLOR_SCHEME_VARIANT]
                        ?: UserPreferences().dynamicSchemeVariant.ordinal]
            )

        }

    private companion object {
        val SOURCE_COLOR = intPreferencesKey("source_color")
        val COLOR_SCHEME_VARIANT = intPreferencesKey("color_scheme_variant")
    }
}
