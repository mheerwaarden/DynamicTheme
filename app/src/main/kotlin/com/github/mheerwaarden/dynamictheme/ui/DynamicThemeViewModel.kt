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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.github.mheerwaarden.dynamictheme.ui.theme.WhiteArgb
import dynamiccolor.Variant
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

    var uiState by mutableStateOf(DynamicThemeUiState())
        protected set

    override suspend fun loadState() {
        Log.d(TAG, "loadState")
        val preferences = userPreferencesRepository.preferences.first()
        uiState = if (preferences.id <= 0) {
            // Initialize from preference values
            createDynamicThemeUiState(
                id = preferences.id,
                name = preferences.name,
                sourceColorArgb = preferences.sourceColor,
                uiColorSchemeVariant = UiColorSchemeVariant.fromVariant(preferences.dynamicSchemeVariant),
                windowWidthSizeClass = uiState.windowWidthSizeClass
            )
        } else {
            // Initialize from database values
            val theme = dynamicThemeRepository.getDynamicTheme(preferences.id)
            if (theme == null) {
                createDynamicThemeUiState(
                    id = 0L,
                    name = preferences.name,
                    sourceColorArgb = preferences.sourceColor,
                    uiColorSchemeVariant = UiColorSchemeVariant.fromVariant(preferences.dynamicSchemeVariant),
                    windowWidthSizeClass = uiState.windowWidthSizeClass
                )
            } else {
                fromDynamicTheme(theme)
            }
        }
    }

    fun updateWindowSizeClass(newWindowSizeClass: WindowSizeClass) {
        // Update only after reconfiguration
        if (uiState.windowWidthSizeClass != newWindowSizeClass.widthSizeClass) {
            uiState =
                    uiState.copy(windowWidthSizeClass = newWindowSizeClass.widthSizeClass)
        }
    }

    fun updateName(newName: String) {
        uiState = uiState.copy(name = newName)
        if (isPreferenceState) {
            setNamePreference(newName)
        }
    }

    fun updateColorScheme(
        sourceColorArgb: Int,
        uiColorSchemeVariant: UiColorSchemeVariant = UiColorSchemeVariant.TonalSpot,
    ) {
        uiState = createDynamicThemeUiState(
            id = uiState.id,
            name = uiState.name,
            sourceColorArgb = sourceColorArgb,
            uiColorSchemeVariant = uiColorSchemeVariant,
            windowWidthSizeClass = uiState.windowWidthSizeClass
        )
        if (isPreferenceState) {
            setSourceColorPreference(
                sourceColorArgb, uiColorSchemeVariant.toVariant()
            )
        }
    }

    /** Insert the dynamicTheme in the database if it does not exist yet, otherwise update it. */
    fun upsertDynamicTheme() {
        if (uiState.id <= 0) {
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
                        dynamicThemeRepository.insertDynamicTheme(uiState.toDynamicTheme())
                uiState = uiState.copy(id = newId)
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
                dynamicThemeRepository.updateDynamicTheme(uiState.toDynamicTheme())
                Log.d(
                    TAG,
                    "updateDynamicTheme: DynamicTheme updated: " + "${uiState.id} - ${uiState.name}"
                )
            } catch (e: Exception) {
                val msg = "updateDynamicTheme: Exception during insert: ${e.message}"
                Log.e(TAG, msg)
                onException(msg)
            }
        }
    }

    /**
     * Delete the dynamicTheme from the database.
     * Pass in the id to let the method be used from the home screen.
     */
    fun deleteDynamicTheme(id: Long) {
        if (id < 0) return

        viewModelScope.launch {
            try {
                dynamicThemeRepository.deleteDynamicThemeById(Id(id))
                if (isPreferenceState) {
                    // Reset preferences
                    userPreferencesRepository.saveIdPreference(0L)
                }
                Log.d(TAG, "deleteDynamicTheme: DynamicTheme deleted: $id")
            } catch (e: Exception) {
                val msg = "deleteDynamicTheme: Exception during delete: ${e.message}"
                Log.e(TAG, msg)
                onException(msg)
            }
        }
    }

    fun resetState() {
        Log.d(TAG, "resetState")
        uiState = DynamicThemeUiState(windowWidthSizeClass = uiState.windowWidthSizeClass)
        if (isPreferenceState) {
            // Resets all preferences
            setIdPreference(0L)
        }
    }

    private fun setIdPreference(id: Long) {
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
    val sourceColorArgb: Int = WhiteArgb,
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
