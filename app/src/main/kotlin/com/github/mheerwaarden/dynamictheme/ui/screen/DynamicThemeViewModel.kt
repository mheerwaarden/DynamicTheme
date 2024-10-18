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

import android.util.Log
import androidx.lifecycle.ViewModel
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.ui.ColorSchemeState
import com.github.mheerwaarden.dynamictheme.ui.theme.DarkColorScheme
import com.github.mheerwaarden.dynamictheme.ui.theme.LightColorScheme
import com.github.mheerwaarden.dynamictheme.ui.toColorSchemeState
import dynamiccolor.Variant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = APP_TAG + "_DynamicThemeViewModel"

/**
 * A view model that is used in all the steps to select a color and a scheme variant
 */
class DynamicThemeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DynamicThemeUiState())
    val uiState: StateFlow<DynamicThemeUiState> = _uiState.asStateFlow()

    fun updateColorScheme(
        sourceColorRgb: Int,
        uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
    ) {
        _uiState.value = createDynamicThemeUiState(sourceColorRgb, uiColorSchemeVariant)
    }

    private fun createDynamicThemeUiState(
        sourceColorArgb: Int,
        uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
    ): DynamicThemeUiState {
        Log.d(TAG, "Create State: Color $sourceColorArgb, Theme $uiColorSchemeVariant")
        val schemeVariant = Variant.entries[uiColorSchemeVariant.ordinal]
        return DynamicThemeUiState(
            sourceColorArgb = sourceColorArgb,
            uiColorSchemeVariant = uiColorSchemeVariant,
            lightColorSchemeState = ColorExtractor.createDynamicColorScheme(
                sourceArgb = sourceColorArgb,
                schemeVariant = schemeVariant,
                isDark = false
            ).toColorSchemeState(),
            darkColorSchemeState = ColorExtractor.createDynamicColorScheme(
                sourceArgb = sourceColorArgb,
                schemeVariant = schemeVariant,
                isDark = true
            ).toColorSchemeState(),
        )
    }
}

data class DynamicThemeUiState(
    val sourceColorArgb: Int = 0,
    val uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
    val lightColorSchemeState: ColorSchemeState = LightColorScheme.toColorSchemeState(),
    val darkColorSchemeState: ColorSchemeState = DarkColorScheme.toColorSchemeState(),
)