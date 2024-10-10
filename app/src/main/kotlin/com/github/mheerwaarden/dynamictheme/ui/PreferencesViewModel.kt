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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val preferencesState: StateFlow<UserPreferences> =
            userPreferencesRepository.preferences.stateIn(
                scope = viewModelScope,
                // Flow is set to emits value for when app is on the foreground
                // The 5 seconds stop delay is added to ensure it flows continuously
                // for cases such as configuration change
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = UserPreferences()
            )

    fun setSourceColorPreference(color: Int) {
        Log.d(APP_TAG, "Setting source color preference: $color")
        viewModelScope.launch {
            userPreferencesRepository.saveSourceColorPreference(color)
            Log.d(APP_TAG, "Saved source color preference: $color")
        }
    }

    companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}