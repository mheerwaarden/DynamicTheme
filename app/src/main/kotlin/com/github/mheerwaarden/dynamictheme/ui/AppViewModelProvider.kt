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

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.mheerwaarden.dynamictheme.DynamicThemeApplication
import com.github.mheerwaarden.dynamictheme.ui.screen.DynamicThemeViewModel
import com.github.mheerwaarden.dynamictheme.ui.screen.ImagePickerViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { PreferencesViewModel(dynamicThemeApplication().userPreferencesRepository) }
        initializer { ImagePickerViewModel() }
        initializer { DynamicThemeViewModel() }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [DynamicThemeApplication].
 */
fun CreationExtras.dynamicThemeApplication(): DynamicThemeApplication =
        (this[AndroidViewModelFactory.APPLICATION_KEY] as DynamicThemeApplication)
