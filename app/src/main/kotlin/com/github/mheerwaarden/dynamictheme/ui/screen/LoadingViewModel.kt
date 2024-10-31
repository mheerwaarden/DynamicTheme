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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mheerwaarden.dynamictheme.APP_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface LoadingState {
    data object Loading : LoadingState
    data object Success : LoadingState
    data class Failure(val error: Throwable) : LoadingState
}

abstract class LoadingViewModel : ViewModel() {
    var loadingState: LoadingState by mutableStateOf(LoadingState.Loading)

    init {
        load()
    }

    /**
     * Initial data load in the uiState with progress indication and error handling.
     */
    fun load() {
        loadingState = LoadingState.Loading
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    loadState()
                }
                loadingState = LoadingState.Success
            } catch (e: Exception) {
                Log.e(APP_TAG, "loadState: Exception during update: ${e.message}")
                loadingState = LoadingState.Failure(e)
            }
        }
    }

    /**
     * Override to load the uiState in the view model. The loadingState is updated by default.
     * The LoadingScreen handles the progress indicator and allows a retry on error messages.
     */
    abstract suspend fun loadState()

}