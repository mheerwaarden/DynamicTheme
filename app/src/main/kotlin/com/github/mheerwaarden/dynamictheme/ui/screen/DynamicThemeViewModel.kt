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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.data.database.DynamicTheme
import com.github.mheerwaarden.dynamictheme.data.database.DynamicThemeRepository
import com.github.mheerwaarden.dynamictheme.data.database.Id
import com.github.mheerwaarden.dynamictheme.data.preferences.UserPreferences
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.ui.ColorSchemeState
import com.github.mheerwaarden.dynamictheme.ui.screen.DynamicThemeUiState.Companion.createDynamicThemeUiState
import com.github.mheerwaarden.dynamictheme.ui.screen.DynamicThemeUiState.Companion.fromDynamicTheme
import com.github.mheerwaarden.dynamictheme.ui.theme.DarkColorScheme
import com.github.mheerwaarden.dynamictheme.ui.theme.LightColorScheme
import com.github.mheerwaarden.dynamictheme.ui.toColorSchemeState
import dynamiccolor.Variant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

private const val TAG = APP_TAG + "_DynamicThemeViewModel"

/**
 * A view model that is used in all the steps to select a color and a scheme variant
 */
open class DynamicThemeViewModel(
    private val dynamicThemeRepository: DynamicThemeRepository,
    var onException: (String) -> Unit = { _ -> },
) : ViewModel() {

    protected val _uiState = MutableStateFlow(DynamicThemeUiState())
    val uiState: StateFlow<DynamicThemeUiState> = _uiState.asStateFlow()

    fun updateWindowSizeClass(newWindowSizeClass: WindowSizeClass) {
        // Update only after reconfiguration
        if (_uiState.value.windowWidthSizeClass != newWindowSizeClass.widthSizeClass) {
            _uiState.value =
                    _uiState.value.copy(windowWidthSizeClass = newWindowSizeClass.widthSizeClass)
        }
    }

    fun updateName(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName)
    }

    fun updateColorScheme(
        sourceColorArgb: Int,
        uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
    ) {
        _uiState.value = createDynamicThemeUiState(
            id = _uiState.value.id,
            name = _uiState.value.name,
            sourceColorArgb = sourceColorArgb,
            uiColorSchemeVariant = uiColorSchemeVariant
        )
    }

    fun updateFromPreferences(preferences: UserPreferences) {
        if (preferences.id < 0) {
            // Initialize from preference values
            _uiState.value = createDynamicThemeUiState(
                id = preferences.id,
                name = preferences.name,
                sourceColorArgb = preferences.sourceColor,
                uiColorSchemeVariant = UiColorSchemeVariant.fromVariant(preferences.dynamicSchemeVariant)
            )
        } else {
            // Initialize from database values
            viewModelScope.launch {
                val dynamicThemeUiState = withContext(Dispatchers.IO) {
                    val theme = dynamicThemeRepository.getDynamicTheme(preferences.id)
                    if (theme == null) {
                        createDynamicThemeUiState(
                            id = -1,
                            name = preferences.name,
                            sourceColorArgb = preferences.sourceColor,
                            uiColorSchemeVariant = UiColorSchemeVariant.fromVariant(preferences.dynamicSchemeVariant)
                        )
                    } else {
                        fromDynamicTheme(theme)
                    }
                }
                _uiState.value = dynamicThemeUiState
            }

        }
    }

    /** Insert the dynamicTheme in the database if it does not exist yet, otherwise update it. */
    fun upsertDynamicTheme() {
        if (_uiState.value.id < 0) {
            insertDynamicTheme()
        } else {
            updateDynamicTheme()
        }
    }

    /** Insert the dynamicTheme in the database and schedule the alarm */
    private fun insertDynamicTheme() {
        viewModelScope.launch {
            try {
                val newId =
                        dynamicThemeRepository.insertDynamicTheme(_uiState.value.toDynamicTheme())
                _uiState.value = _uiState.value.copy(id = newId)
                Log.d(TAG, "insertDynamicTheme: DynamicTheme added: $newId")
            } catch (e: Exception) {
                val msg = "addDynamicTheme: Exception during insert: ${e.message}"
                Log.e(TAG, msg)
                onException(msg)
            }
        }
    }

    /** Update the dynamicTheme in the database. */
    private fun updateDynamicTheme() {
        viewModelScope.launch {
            try {
                dynamicThemeRepository.updateDynamicTheme(_uiState.value.toDynamicTheme())
                Log.d(
                    TAG,
                    "updateAddDynamicTheme: DynamicTheme updated: " + "${_uiState.value.id} ${_uiState.value.name}"
                )
            } catch (e: Exception) {
                val msg = "updateAddDynamicTheme: Exception during insert: ${e.message}"
                Log.e(TAG, msg)
                onException(msg)
            }
        }
    }

    /** Delete the dynamicTheme from the database. */
    fun deleteDynamicTheme() {
        viewModelScope.launch {
            try {
                dynamicThemeRepository.deleteDynamicThemeById(Id(_uiState.value.id))
            } catch (e: Exception) {
                val msg = "deleteDynamicTheme: Exception during delete: ${e.message}"
                Log.e(TAG, msg)
                onException(msg)
            }
        }
    }

    companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}

data class DynamicThemeUiState(
    val id: Long = -1,
    val name: String = "",
    val sourceColorArgb: Int = 0,
    val uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
    val lightColorSchemeState: ColorSchemeState = LightColorScheme.toColorSchemeState(),
    val darkColorSchemeState: ColorSchemeState = DarkColorScheme.toColorSchemeState(),

    val windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
) {
    fun toDynamicTheme(): DynamicTheme = DynamicTheme(
        id = if (id < 0) 0 else id,
        name = name,
        sourceArgb = sourceColorArgb,
        colorSchemeVariant = uiColorSchemeVariant.toVariant(),
        primaryArgb = lightColorSchemeState.primary,
        onPrimaryArgb = lightColorSchemeState.onPrimary,
        secondaryArgb = lightColorSchemeState.secondary,
        onSecondaryArgb = lightColorSchemeState.onSecondary,
        tertiaryArgb = lightColorSchemeState.tertiary,
        onTertiaryArgb = lightColorSchemeState.onTertiary,
        surfaceArgb = lightColorSchemeState.surface,
        onSurfaceArgb = lightColorSchemeState.onSurface,
        surfaceVariantArgb = lightColorSchemeState.surfaceVariant,
        onSurfaceVariantArgb = lightColorSchemeState.onSurfaceVariant,
        errorArgb = lightColorSchemeState.error,
        onErrorArgb = lightColorSchemeState.onError,
        timestamp = LocalDateTime.now()
    )

    fun isHorizontalLayout(): Boolean = windowWidthSizeClass != WindowWidthSizeClass.Compact

    companion object {
        fun fromDynamicTheme(dynamicTheme: DynamicTheme): DynamicThemeUiState =
                createDynamicThemeUiState(
                    id = dynamicTheme.id,
                    name = dynamicTheme.name,
                    sourceColorArgb = dynamicTheme.sourceArgb,
                    uiColorSchemeVariant = UiColorSchemeVariant.fromVariant(dynamicTheme.colorSchemeVariant)
                )

        fun createDynamicThemeUiState(
            sourceColorArgb: Int,
            id: Long = -1,
            name: String = "",
            uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
        ): DynamicThemeUiState {
            Log.d(TAG, "Create State: Color $sourceColorArgb, Theme $uiColorSchemeVariant")
            val schemeVariant = Variant.entries[uiColorSchemeVariant.ordinal]
            return DynamicThemeUiState(
                id = id,
                name = name,
                sourceColorArgb = sourceColorArgb,
                uiColorSchemeVariant = uiColorSchemeVariant,
                lightColorSchemeState = ColorExtractor.createDynamicColorScheme(
                    sourceArgb = sourceColorArgb, schemeVariant = schemeVariant, isDark = false
                ).toColorSchemeState(),
                darkColorSchemeState = ColorExtractor.createDynamicColorScheme(
                    sourceArgb = sourceColorArgb, schemeVariant = schemeVariant, isDark = true
                ).toColorSchemeState(),
            )
        }
    }
}