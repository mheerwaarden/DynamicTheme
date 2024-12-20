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

package com.github.mheerwaarden.dynamictheme.ui.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.data.database.DynamicTheme
import com.github.mheerwaarden.dynamictheme.data.database.DynamicThemeRepository
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeViewModel.Companion.TIMEOUT_MILLIS
import com.github.mheerwaarden.dynamictheme.ui.screen.LoadingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to retrieve all dynamicThemes in the Room database.
 */
class HomeViewModel(
    private val dynamicThemesRepository: DynamicThemeRepository,
) : LoadingViewModel() {
    var homeState: StateFlow<List<DynamicTheme>> = MutableStateFlow(listOf())

    override suspend fun loadState() {
        Log.d(APP_TAG, "HomeViewModel: loadState")
        homeState =
                dynamicThemesRepository.getAllDynamicThemesStream().stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                    initialValue = listOf()
                )
    }

}
