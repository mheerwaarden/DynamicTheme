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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.data.database.DynamicTheme
import com.github.mheerwaarden.dynamictheme.data.database.DynamicThemeRepository
import com.github.mheerwaarden.dynamictheme.data.database.Id
import com.github.mheerwaarden.dynamictheme.data.preferences.UserPreferencesRepository
import com.github.mheerwaarden.dynamictheme.material.color.utils.ColorExtractor
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState.Companion.createDynamicThemeUiState
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState.Companion.fromDynamicTheme
import com.github.mheerwaarden.dynamictheme.ui.screen.LoadingViewModel
import com.github.mheerwaarden.dynamictheme.ui.screen.UiColorSchemeVariant
import com.github.mheerwaarden.dynamictheme.ui.theme.DarkColorScheme
import com.github.mheerwaarden.dynamictheme.ui.theme.LightColorScheme
import dynamiccolor.Variant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private const val TAG = APP_TAG + "_DynamicThemeViewModel"

/**
 * A view model that is used in all the steps to select a color and a scheme variant
 */
open class DynamicThemeViewModel(
    private val dynamicThemeRepository: DynamicThemeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val isPreferenceState: Boolean = true,
    var onException: (String) -> Unit = { _ -> },
) : LoadingViewModel() {

    protected val _uiState = MutableStateFlow(DynamicThemeUiState())
    val uiState: StateFlow<DynamicThemeUiState> = _uiState.asStateFlow()

    override suspend fun loadState() {
        val preferences = userPreferencesRepository.preferences.first()
        _uiState.value = if (preferences.id < 0) {
            // Initialize from preference values
            createDynamicThemeUiState(
                id = preferences.id,
                name = preferences.name,
                sourceColorArgb = preferences.sourceColor,
                uiColorSchemeVariant = UiColorSchemeVariant.fromVariant(preferences.dynamicSchemeVariant),
                windowWidthSizeClass = _uiState.value.windowWidthSizeClass
            )
        } else {
            // Initialize from database values
            val theme = dynamicThemeRepository.getDynamicTheme(preferences.id)
            if (theme == null) {
                createDynamicThemeUiState(
                    id = -1,
                    name = preferences.name,
                    sourceColorArgb = preferences.sourceColor,
                    uiColorSchemeVariant = UiColorSchemeVariant.fromVariant(preferences.dynamicSchemeVariant),
                    windowWidthSizeClass = _uiState.value.windowWidthSizeClass
                )
            } else {
                fromDynamicTheme(theme)
            }
        }
    }

    fun updateWindowSizeClass(newWindowSizeClass: WindowSizeClass) {
        // Update only after reconfiguration
        if (_uiState.value.windowWidthSizeClass != newWindowSizeClass.widthSizeClass) {
            _uiState.value =
                    _uiState.value.copy(windowWidthSizeClass = newWindowSizeClass.widthSizeClass)
        }
    }

    fun updateName(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName)
        if (isPreferenceState) {
            setNamePreference(newName)
        }
    }

    fun updateColorScheme(
        sourceColorArgb: Int,
        uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
    ) {
        _uiState.value = createDynamicThemeUiState(
            id = _uiState.value.id,
            name = _uiState.value.name,
            sourceColorArgb = sourceColorArgb,
            uiColorSchemeVariant = uiColorSchemeVariant,
            windowWidthSizeClass = _uiState.value.windowWidthSizeClass
        )
        if (isPreferenceState) {
            setSourceColorPreference(
                sourceColorArgb, uiColorSchemeVariant.toVariant()
            )
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
                if (isPreferenceState) {
                    setIdPreference(newId)
                }
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
        if (_uiState.value.id < 0) return

        viewModelScope.launch {
            try {
                dynamicThemeRepository.deleteDynamicThemeById(Id(_uiState.value.id))
                if (isPreferenceState) {
                    // Reset preferences
                    userPreferencesRepository.saveIdPreference(0L)
                }
            } catch (e: Exception) {
                val msg = "deleteDynamicTheme: Exception during delete: ${e.message}"
                Log.e(TAG, msg)
                onException(msg)
            }
        }
    }

    fun resetState() {
        _uiState.value = DynamicThemeUiState()
        if (isPreferenceState) {
            // Reset preferences
            setIdPreference(0L)
        }
    }

    fun setIdPreference(id: Long) {
        Log.d(APP_TAG, "DynamicThemeViewModel: Setting id preference: $id")
        viewModelScope.launch {
            userPreferencesRepository.saveIdPreference(id)
        }
    }

    private fun setNamePreference(name: String) {
        Log.d(APP_TAG, "DynamicThemeViewModel: Setting name preference: $name")
        viewModelScope.launch {
            userPreferencesRepository.saveNamePreference(name)
        }
    }

    private fun setSourceColorPreference(color: Int, schemeVariant: Variant) {
        Log.d(APP_TAG, "DynamicThemeViewModel: Setting source color preference: $color")
        viewModelScope.launch {
            userPreferencesRepository.saveSourceColorPreference(color, schemeVariant)
        }
    }

    companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}

data class DynamicThemeUiState(
    val id: Long = 0L,
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

    @Composable
    fun toColorScheme(): ColorScheme = if (isSystemInDarkTheme()) {
        darkColorSchemeState.toColorScheme()
    } else {
        lightColorSchemeState.toColorScheme()
    }

    fun isHorizontalLayout(): Boolean = windowWidthSizeClass != WindowWidthSizeClass.Compact

    companion object {
        fun fromDynamicTheme(
            dynamicTheme: DynamicTheme,
            windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
        ): DynamicThemeUiState = createDynamicThemeUiState(
            id = dynamicTheme.id,
            name = dynamicTheme.name,
            sourceColorArgb = dynamicTheme.sourceArgb,
            uiColorSchemeVariant = UiColorSchemeVariant.fromVariant(dynamicTheme.colorSchemeVariant),
            windowWidthSizeClass = windowWidthSizeClass
        )

        fun createDynamicThemeUiState(
            sourceColorArgb: Int,
            windowWidthSizeClass: WindowWidthSizeClass,
            id: Long = 0L,
            name: String = "",
            uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
        ): DynamicThemeUiState {
            Log.d(
                TAG, "Create State: $id - $name Color $sourceColorArgb, Theme $uiColorSchemeVariant"
            )
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
                windowWidthSizeClass = windowWidthSizeClass
            )
        }
    }
}
