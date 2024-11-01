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

package com.github.mheerwaarden.dynamictheme.ui.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.dynamictheme.data.database.DynamicThemeRepository
import com.github.mheerwaarden.dynamictheme.data.preferences.UserPreferencesRepository
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeViewModel
import kotlinx.coroutines.launch

class DynamicThemeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    dynamicThemeRepository: DynamicThemeRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : DynamicThemeViewModel(
    dynamicThemeRepository = dynamicThemeRepository,
    userPreferencesRepository = userPreferencesRepository,
    isPreferenceState = false
) {
    private val themeId: Long =
            checkNotNull(savedStateHandle[DynamicThemeDetailDestination.themeIdArg])

    init {
        viewModelScope.launch {
            val dynamicTheme = dynamicThemeRepository.getDynamicTheme(themeId)
            if (dynamicTheme != null) {
                uiState = DynamicThemeUiState.fromDynamicTheme(
                    dynamicTheme = dynamicTheme,
                )
            }
        }
    }

}