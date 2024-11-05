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

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.dynamictheme.APP_TAG
import com.github.mheerwaarden.dynamictheme.data.database.DynamicThemeRepository
import com.github.mheerwaarden.dynamictheme.data.database.Id
import com.github.mheerwaarden.dynamictheme.data.preferences.UserPreferencesRepository
import com.github.mheerwaarden.dynamictheme.export.exportColorKotlin
import com.github.mheerwaarden.dynamictheme.export.exportThemeKotlin
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState.Companion.createDynamicThemeUiState
import com.github.mheerwaarden.dynamictheme.ui.DynamicThemeUiState.Companion.fromDynamicTheme
import com.github.mheerwaarden.dynamictheme.ui.screen.LoadingViewModel
import com.github.mheerwaarden.dynamictheme.ui.screen.UiColorSchemeVariant
import com.github.mheerwaarden.dynamictheme.util.Zipper
import dynamiccolor.Variant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = APP_TAG + "_DynamicThemeViewModel"

/**
 * A view model that is used in all the steps to select a color and a scheme variant
 */
open class DynamicThemeViewModel(
    private val dynamicThemeRepository: DynamicThemeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val isPreferenceState: Boolean = true,
) : LoadingViewModel() {

    var uiState by mutableStateOf(DynamicThemeUiState())
        protected set

    private val _saveResult = MutableSharedFlow<ActionResultState>()
    val saveResult = _saveResult.asSharedFlow()
    private val _exportResult = MutableSharedFlow<ActionResultState>()
    val exportResult = _exportResult.asSharedFlow()
    private val _deleteResult = MutableSharedFlow<ActionResultState>()
    val deleteResult = _deleteResult.asSharedFlow()

    init {
        _saveResult.tryEmit(ActionResult.None)
        _exportResult.tryEmit(ActionResult.None)
    }

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
            uiState = uiState.copy(windowWidthSizeClass = newWindowSizeClass.widthSizeClass)
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
            setSourceColorPreference(sourceColorArgb, uiColorSchemeVariant.toVariant())
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
            _saveResult.emit(ActionResult.Busy)
            try {
                val newId = dynamicThemeRepository.insertDynamicTheme(uiState.toDynamicTheme())
                uiState = uiState.copy(id = newId)
                if (isPreferenceState) {
                    setIdPreference(newId)
                }
                Log.d(TAG, "insertDynamicTheme: DynamicTheme added: $newId")
                _saveResult.emit(ActionResult.Success)
            } catch (e: Exception) {
                Log.e(TAG, "addDynamicTheme: Exception during insert: ${e.message}")
                _saveResult.emit(ActionResult.Failure(e))
            }
        }
    }

    /** Update the dynamicTheme in the database. */
    private fun updateDynamicTheme() {
        viewModelScope.launch {
            _saveResult.emit(ActionResult.Busy)
            try {
                dynamicThemeRepository.updateDynamicTheme(uiState.toDynamicTheme())
                Log.d(
                    TAG,
                    "updateDynamicTheme: DynamicTheme updated: " + "${uiState.id} - ${uiState.name}"
                )
                _saveResult.emit(ActionResult.Success)
            } catch (e: Exception) {
                Log.e(TAG, "updateDynamicTheme: Exception during insert: ${e.message}")
                _saveResult.emit(ActionResult.Failure(e))
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
            _deleteResult.emit(DeleteResult.Busy(id))
            try {
                dynamicThemeRepository.deleteDynamicThemeById(Id(id))
                resetState()
                Log.d(TAG, "deleteDynamicTheme: DynamicTheme deleted: $id")
                _deleteResult.emit(DeleteResult.Success(id))
            } catch (e: Exception) {
                val msg = "deleteDynamicTheme: Exception during delete of theme $id: ${e.message}"
                Log.e(TAG, msg)
                _deleteResult.emit(DeleteResult.Failure(id, e))
            }
        }
    }

    fun resetState() {
        Log.d(TAG, "resetState")
        uiState = DynamicThemeUiState(windowWidthSizeClass = uiState.windowWidthSizeClass)
        if (isPreferenceState) {
            // Resets all preferences
            resetPreference()
        }
    }

    private fun resetPreference() {
        Log.d(APP_TAG, "DynamicThemeViewModel: Resetting preference")
        viewModelScope.launch {
            userPreferencesRepository.reset()
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

    fun exportDynamicTheme(context: Context) {
        viewModelScope.launch { withContext(Dispatchers.IO) {
            try {
                _exportResult.emit(ActionResult.Busy)

                val name = uiState.name
                val zipFileName = "${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/${name}Theme.zip"
                Log.d(TAG, "exportDynamicTheme: Exporting to $zipFileName")
                Zipper(zipFileName).use { zipper ->
                    zipper.add(
                        fileName = "${name}Color.kt",
                        data = exportColorKotlin(
                            sourceColorArgb = uiState.sourceColorArgb,
                            schemeVariant = uiState.uiColorSchemeVariant.toVariant()
                        )
                    )
                    zipper.add(fileName = "${name}Theme.kt", data = exportThemeKotlin)
                }
                _exportResult.emit(ActionResult.Success)
            } catch (e: Exception) {
                val msg = "exportDynamicTheme: Exception during export: ${e.message}"
                Log.e(TAG, msg)
                _exportResult.emit(ActionResult.Failure(e))
            }
        }}
    }

    companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}

