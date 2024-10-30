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

package com.github.mheerwaarden.dynamictheme.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.data.preferences.UserPreferences
import com.github.mheerwaarden.dynamictheme.data.preferences.UserPreferencesRepository
import com.github.mheerwaarden.dynamictheme.ui.screen.DynamicThemeViewModel
import dynamiccolor.Variant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The preference keeps the latest created theme, so it remains available on restart even without
 * saving to the database.
 */
class PreferencesViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val preferencesState: StateFlow<UserPreferences> =
            userPreferencesRepository.preferences.stateIn(
                scope = viewModelScope,
                // The 5 seconds stop delay is added to ensure it flows continuously
                // for cases such as configuration change
                started = SharingStarted.WhileSubscribed(DynamicThemeViewModel.TIMEOUT_MILLIS),
                initialValue = UserPreferences()
            )

    fun setIdPreference(id: Long) {
        Log.d(APP_TAG, "PreferencesViewModel: Setting id preference: $id")
        viewModelScope.launch {
            userPreferencesRepository.saveIdPreference(id)
        }
    }

    fun setNamePreference(name: String) {
        Log.d(APP_TAG, "PreferencesViewModel: Setting name preference: $name")
        viewModelScope.launch {
            userPreferencesRepository.saveNamePreference(name)
        }
    }

    fun setSourceColorPreference(color: Int, schemeVariant: Variant) {
        Log.d(APP_TAG, "PreferencesViewModel: Setting source color preference: $color")
        viewModelScope.launch {
            userPreferencesRepository.saveSourceColorPreference(color, schemeVariant)
            Log.d(APP_TAG, "Saved source color preference: $color")
        }
    }
    
}
