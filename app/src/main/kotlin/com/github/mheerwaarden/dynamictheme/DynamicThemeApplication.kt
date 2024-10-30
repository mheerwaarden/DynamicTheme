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

package com.github.mheerwaarden.dynamictheme

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.github.mheerwaarden.dynamictheme.data.AppContainer
import com.github.mheerwaarden.dynamictheme.data.AppDataContainer
import com.github.mheerwaarden.dynamictheme.data.preferences.UserPreferencesDataStoreRepository

private const val DYNAMIC_THEME_PREFERENCES_NAME = "dynamic_theme_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DYNAMIC_THEME_PREFERENCES_NAME,
)

class DynamicThemeApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesDataStoreRepository

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        userPreferencesRepository = UserPreferencesDataStoreRepository(dataStore)
        container = AppDataContainer(this)
    }
}